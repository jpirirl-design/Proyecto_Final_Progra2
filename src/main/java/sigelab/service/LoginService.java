package sigelab.service;

import sigelab.config.AppConfig;
import sigelab.enums.AccionAuditoria;
import sigelab.exception.BusinessException;
import sigelab.exception.DataAccessException;
import sigelab.facial.ClienteFacial;
import sigelab.facial.ResultadoFacial;
import sigelab.repository.AuditoriaRepository;
import sigelab.repository.LoginRepository;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;

public class LoginService {

    private final LoginRepository repository;
    private final ClienteFacial clienteFacial;
    private final AuditoriaRepository auditoriaRepository;

    public LoginService() {
        this(new LoginRepository(), new ClienteFacial(), new AuditoriaRepository());
    }

    public LoginService(LoginRepository repository,
                        ClienteFacial clienteFacial,
                        AuditoriaRepository auditoriaRepository) {
        this.repository = repository;
        this.clienteFacial = clienteFacial;
        this.auditoriaRepository = auditoriaRepository;
    }

    // VALIDACIONES ORIGINALES
    public String validarCredenciales(
            String usuario,
            char[] password
    ) {

        if (usuario == null || usuario.trim().isEmpty()) {
            return "Debe ingresar su usuario.";
        }

        if (password == null || password.length == 0) {
            return "Debe ingresar su contraseña.";
        }

        return null;
    }

    /**
     * Login en dos factores:
     * 1) usuario + contraseña + estado ACTIVO (RN-01)
     * 2) validación facial obligatoria con prueba de vida (RN-02)
     * Cada intento queda en Auditoría (RN-18).
     */
    public Object[] iniciarSesion(
            String usuario,
            char[] password
    ) throws SQLException {

        Object[] datos = comprobarCredenciales(usuario, password);
        Integer idUsuario = ((Number) datos[0]).intValue();

        // ---- MODO DESARROLLO: entrar sin reconocimiento facial ----
        // Activar con facial.habilitado=false en config/application.properties.
        // QUITARLO o ponerlo en true para la defensa (RN-02).
        if ("false".equalsIgnoreCase(AppConfig.get("facial.habilitado", "true"))) {
            auditar(idUsuario, AccionAuditoria.LOGIN_EXITOSO,
                    "Login SIN validación facial (modo desarrollo).");
            return new Object[]{
                    datos[0], // ID
                    datos[1], // Nombre
                    datos[2], // Apellido
                    datos[3], // Usuario
                    datos[6]  // Rol
            };
        }

        // ---- Segundo factor: reconocimiento facial (RN-02) ----
        ResultadoFacial facial;
        try {
            facial = clienteFacial.verificar(idUsuario);
        } catch (BusinessException ex) {
            auditar(idUsuario, AccionAuditoria.LOGIN_FALLIDO,
                    "Validación facial no completada: " + ex.getMessage());
            throw new IllegalArgumentException(ex.getMessage());
        }

        if (!facial.coincide() || !facial.livenessAprobado()) {
            auditar(idUsuario, AccionAuditoria.LOGIN_FALLIDO,
                    "Validación facial rechazada.");
            throw new IllegalArgumentException(
                    "Rostro no reconocido. Acceso denegado."
            );
        }

        auditar(idUsuario, AccionAuditoria.LOGIN_EXITOSO,
                "Contraseña y rostro verificados.");

        // No devolver el hash de contraseña.
        return new Object[]{
                datos[0], // ID
                datos[1], // Nombre
                datos[2], // Apellido
                datos[3], // Usuario
                datos[6]  // Rol
        };
    }

    /**
     * Primer registro de rostro desde el Login: exige usuario y contraseña correctos
     * y solo funciona si la persona todavía no tiene perfil facial.
     * Para reemplazar un rostro ya registrado se necesita un ADMIN (pantalla Usuarios).
     */
    public String registrarRostroInicial(
            String usuario,
            char[] password
    ) throws SQLException {

        Object[] datos = comprobarCredenciales(usuario, password);
        int idUsuario = ((Number) datos[0]).intValue();

        if (clienteFacial.estaRegistrado(idUsuario)) {
            throw new IllegalArgumentException(
                    "El rostro ya está registrado. Solicite a un ADMIN su actualización."
            );
        }

        return clienteFacial.registrar(idUsuario, false);
    }

    /** Primer factor: existe, está ACTIVO y la contraseña coincide. Devuelve la fila del usuario. */
    private Object[] comprobarCredenciales(
            String usuario,
            char[] password
    ) throws SQLException {

        String error = validarCredenciales(usuario, password);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        String nombreUsuario = usuario.trim();
        Object[] datos = repository.buscarPorUsuario(nombreUsuario);

        if (datos == null) {
            auditar(null, AccionAuditoria.LOGIN_FALLIDO,
                    "Usuario inexistente: " + nombreUsuario);
            throw new IllegalArgumentException(
                    "Usuario o contraseña incorrectos."
            );
        }

        Integer idUsuario = ((Number) datos[0]).intValue();
        String estado = String.valueOf(datos[5]);

        if (!"ACTIVO".equalsIgnoreCase(estado)) {                       // RN-01
            auditar(idUsuario, AccionAuditoria.LOGIN_FALLIDO, "Usuario INACTIVO.");
            throw new IllegalArgumentException(
                    "Este usuario está inactivo."
            );
        }

        String hashGuardado = String.valueOf(datos[4]);

        if (!verificarPassword(password, hashGuardado)) {
            auditar(idUsuario, AccionAuditoria.LOGIN_FALLIDO, "Contraseña incorrecta.");
            throw new IllegalArgumentException(
                    "Usuario o contraseña incorrectos."
            );
        }

        return datos;
    }

    /** Un fallo al auditar no debe ocultar el motivo real del rechazo. */
    private void auditar(Integer idUsuario, AccionAuditoria accion, String descripcion) {
        try {
            auditoriaRepository.registrar(idUsuario, accion.name(), "Usuario",
                    idUsuario, descripcion);
        } catch (DataAccessException ex) {
            System.err.println("No se pudo auditar " + accion + ": " + ex.getMessage());
        }
    }

    // VERIFICAR CONTRASEÑA PBKDF2
    private boolean verificarPassword(
            char[] password,
            String hashGuardado
    ) {

        if (hashGuardado == null) {
            return false;
        }

        try {
            String[] partes = hashGuardado.split("\\$");

            if (partes.length != 4 ||
                    !"pbkdf2_sha256".equals(partes[0])) {
                return false;
            }

            int iteraciones = Integer.parseInt(partes[1]);

            if (iteraciones < 100000 || iteraciones > 2000000) {
                return false;
            }

            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] hashEsperado =
                    Base64.getDecoder().decode(partes[3]);

            if (sal.length != 16 || hashEsperado.length != 32) {
                return false;
            }

            PBEKeySpec especificacion = new PBEKeySpec(
                    password,
                    sal,
                    iteraciones,
                    hashEsperado.length * 8
            );

            try {
                SecretKeyFactory fabrica =
                        SecretKeyFactory.getInstance(
                                "PBKDF2WithHmacSHA256"
                        );

                byte[] hashCalculado =
                        fabrica.generateSecret(
                                especificacion
                        ).getEncoded();

                try {
                    return MessageDigest.isEqual(
                            hashEsperado,
                            hashCalculado
                    );
                } finally {
                    Arrays.fill(hashCalculado, (byte) 0);
                }

            } finally {
                especificacion.clearPassword();
                Arrays.fill(sal, (byte) 0);
                Arrays.fill(hashEsperado, (byte) 0);
            }

        } catch (Exception ex) {
            return false;
        }
    }
}