
package sigelab.service;

public class UsuarioService {

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
}
