
package sigelab.service;

public class LoginService {

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
}
