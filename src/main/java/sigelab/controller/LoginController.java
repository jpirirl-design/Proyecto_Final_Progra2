
package sigelab.controller;

import sigelab.service.LoginService;

public class LoginController {

    private final LoginService service;

    public LoginController() {
        service = new LoginService();
    }

    public String validarCredenciales(
            String usuario,
            char[] password
    ) {
        return service.validarCredenciales(usuario, password);
    }
}
