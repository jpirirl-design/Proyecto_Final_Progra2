
package sigelab.controller;

import sigelab.service.UsuarioService;

public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController() {
        service = new UsuarioService();
    }

    public String validarUsuario(
            String nombre,
            String apellido,
            String usuario,
            String correo,
            char[] password,
            String rol
    ) {
        return service.validarUsuario(
                nombre,
                apellido,
                usuario,
                correo,
                password,
                rol
        );
    }
}
