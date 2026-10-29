
package sigelab.controller;

import sigelab.service.LoginService;

import java.sql.SQLException;

public class LoginController {

    private final LoginService service;

    public LoginController() {
        service = new LoginService();
    }

    // VALIDAR USUARIO Y CONTRASEÑA
    public String validarCredenciales(
            String usuario,
            char[] password
    ) {
        return service.validarCredenciales(
                usuario,
                password
        );
    }

    // INICIAR SESIÓN CON SQL SERVER
    public Object[] iniciarSesion(
            String usuario,
            char[] password
    ) throws SQLException {

        return service.iniciarSesion(
                usuario,
                password
        );
    }
}
