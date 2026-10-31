
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

    // REGISTRAR ROSTRO POR PRIMERA VEZ (con usuario y contraseña)
    public String registrarRostroInicial(
            String usuario,
            char[] password
    ) throws SQLException {
        return service.registrarRostroInicial(usuario, password);
    }

    // CREAR ADMINISTRADOR INICIAL (solo si no existen usuarios)
    public void crearAdministradorInicial(
            String nombre,
            String apellido,
            String usuario,
            String correo,
            char[] password
    ) throws SQLException {
        new sigelab.service.UsuarioService().crearAdministradorInicial(
                nombre, apellido, usuario, correo, password
        );
    }
}
