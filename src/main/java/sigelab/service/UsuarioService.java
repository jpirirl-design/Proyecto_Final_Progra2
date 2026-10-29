
package sigelab.service;

import sigelab.repository.UsuarioRepository;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class UsuarioService {

    private final UsuarioRepository repository =
            new UsuarioRepository();

    private static final int ITERACIONES = 600000;
    private static final int LONGITUD_SAL = 16;
    private static final int LONGITUD_HASH = 256;

    // VALIDACIONES ORIGINALES
    public String validarUsuario(
            String nombre,
            String apellido,
            String usuario,
            String correo,
            char[] password,
            String rol
    ) {

        if (nombre == null || nombre.trim().isEmpty()) {
            return "Debe ingresar el nombre.";
        }

        if (apellido == null || apellido.trim().isEmpty()) {
            return "Debe ingresar el apellido.";
        }

        if (usuario == null || usuario.trim().isEmpty()) {
            return "Debe ingresar el nombre de usuario.";
        }

        if (correo == null ||
                !correo.matches("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            return "Debe ingresar un correo electrónico válido.";
        }

        if (password == null || password.length < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }

        if (rol == null || rol.equals("Seleccione rol")) {
            return "Debe seleccionar un rol.";
        }

        return null;
    }

    // LISTAR USUARIOS
    public List<Object[]> listar() throws SQLException {
        return repository.listar();
    }

    // CONSULTAR ROLES
    public List<Object[]> listarRoles() throws SQLException {
        return repository.listarRoles();
    }

    // GUARDAR USUARIO
    public void guardarUsuario(
            String nombre,
            String apellido,
            String usuario,
            String correo,
            char[] password,
            int idRol,
            String rol,
            String estado
    ) throws SQLException {

        String error = validarUsuario(
                nombre, apellido, usuario,
                correo, password, rol
        );

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        validarRolYEstado(idRol, estado);

        String hash = generarHash(password);

        repository.guardar(
                nombre.trim(),
                apellido.trim(),
                usuario.trim(),
                correo.trim(),
                hash,
                idRol,
                estado
        );
    }

    // EDITAR USUARIO
    // La contraseña es opcional al editar.
    public void editarUsuario(
            int id,
            String nombre,
            String apellido,
            String usuario,
            String correo,
            char[] password,
            int idRol,
            String rol,
            String estado
    ) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un usuario."
            );
        }

        // Conserva las validaciones existentes.
        // Si no se cambia la contraseña, se usa una
        // contraseña temporal solo para validar.
        char[] passwordValidacion =
                (password == null || password.length == 0)
                        ? "Temporal123".toCharArray()
                        : password;

        try {
            String error = validarUsuario(
                    nombre, apellido, usuario,
                    correo, passwordValidacion, rol
            );

            if (error != null) {
                throw new IllegalArgumentException(error);
            }
        } finally {
            if (passwordValidacion != password) {
                Arrays.fill(passwordValidacion, '\0');
            }
        }

        validarRolYEstado(idRol, estado);

        // Actualizar datos generales
        repository.editar(
                id,
                nombre.trim(),
                apellido.trim(),
                usuario.trim(),
                correo.trim(),
                idRol,
                estado
        );

        // Actualizar contraseña únicamente si se ingresó.
        if (password != null && password.length > 0) {
            repository.actualizarPassword(
                    id,
                    generarHash(password)
            );
        }
    }

    // DESACTIVAR USUARIO
    public void desactivarUsuario(int id)
            throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un usuario."
            );
        }

        repository.desactivar(id);
    }

    // VALIDAR ROL Y ESTADO
    private void validarRolYEstado(
            int idRol,
            String estado
    ) {

        if (idRol <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un rol."
            );
        }

        if (!List.of("ACTIVO", "INACTIVO").contains(estado)) {
            throw new IllegalArgumentException(
                    "El estado del usuario no es válido."
            );
        }
    }

    // GENERAR HASH SEGURO DE CONTRASEÑA
    private String generarHash(char[] password) {

        byte[] sal = new byte[LONGITUD_SAL];
        new SecureRandom().nextBytes(sal);

        PBEKeySpec especificacion = new PBEKeySpec(
                password,
                sal,
                ITERACIONES,
                LONGITUD_HASH
        );

        try {
            SecretKeyFactory fabrica =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            byte[] hash =
                    fabrica.generateSecret(
                            especificacion
                    ).getEncoded();

            try {
                return "pbkdf2_sha256$"
                        + ITERACIONES + "$"
                        + Base64.getEncoder()
                        .encodeToString(sal)
                        + "$"
                        + Base64.getEncoder()
                        .encodeToString(hash);
            } finally {
                Arrays.fill(hash, (byte) 0);
            }

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "No se pudo proteger la contraseña.",
                    ex
            );
        } finally {
            especificacion.clearPassword();
            Arrays.fill(sal, (byte) 0);
        }
    }
}
