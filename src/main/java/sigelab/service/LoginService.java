
package sigelab.service;

import sigelab.repository.LoginRepository;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;

public class LoginService {

    private final LoginRepository repository =
            new LoginRepository();

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

    // AUTENTICAR USUARIO CONTRA SQL SERVER
    public Object[] iniciarSesion(
            String usuario,
            char[] password
    ) throws SQLException {

        String error = validarCredenciales(usuario, password);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        Object[] datos = repository.buscarPorUsuario(
                usuario.trim()
        );

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Usuario o contraseña incorrectos."
            );
        }

        String estado = String.valueOf(datos[5]);

        if (!"ACTIVO".equalsIgnoreCase(estado)) {
            throw new IllegalArgumentException(
                    "Este usuario está inactivo."
            );
        }

        String hashGuardado = String.valueOf(datos[4]);

        if (!verificarPassword(password, hashGuardado)) {
            throw new IllegalArgumentException(
                    "Usuario o contraseña incorrectos."
            );
        }

        // No devolver el hash de contraseña.
        return new Object[]{
                datos[0], // ID
                datos[1], // Nombre
                datos[2], // Apellido
                datos[3], // Usuario
                datos[6]  // Rol
        };
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
