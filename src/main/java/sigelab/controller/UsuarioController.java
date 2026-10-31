
package sigelab.controller;

import sigelab.service.UsuarioService;

import java.sql.SQLException;
import java.util.List;

public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController() {
        service = new UsuarioService();
    }

    // VALIDAR DATOS DEL USUARIO
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

    // CONSULTAR TODOS LOS USUARIOS
    public List<Object[]> listar() throws SQLException {
        return service.listar();
    }

    // CONSULTAR ROLES DISPONIBLES
    public List<Object[]> listarRoles() throws SQLException {
        return service.listarRoles();
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

        service.guardarUsuario(
                nombre,
                apellido,
                usuario,
                correo,
                password,
                idRol,
                rol,
                estado
        );
    }

    // EDITAR USUARIO
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

        service.editarUsuario(
                id,
                nombre,
                apellido,
                usuario,
                correo,
                password,
                idRol,
                rol,
                estado
        );
    }

    // DESACTIVAR USUARIO
    public void desactivarUsuario(int id)
            throws SQLException {

        service.desactivarUsuario(id);
    }

    // REGISTRAR ROSTRO PARA EL LOGIN FACIAL
    public String registrarRostro(int idUsuario) {
        return service.registrarRostro(idUsuario);
    }
}
