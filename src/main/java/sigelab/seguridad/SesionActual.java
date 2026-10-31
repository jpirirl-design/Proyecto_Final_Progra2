package sigelab.seguridad;

import sigelab.enums.NombreRol;
import sigelab.exception.BusinessException;
import sigelab.model.Rol;
import sigelab.model.Usuario;

/** Guarda al usuario que inició sesión para que las pantallas puedan registrar quién realiza cada operación. */
public final class SesionActual {

    private static Usuario usuario;

    private SesionActual() {
    }

    /** Construye el Usuario a partir del arreglo que devuelve LoginService.iniciarSesion: {id, nombre, apellido, usuario, rol}. */
    public static void iniciarDesdeLogin(Object[] datos) {
        if (datos == null || datos.length < 5) {
            throw new BusinessException("No se pudo iniciar la sesión.");
        }
        NombreRol nombreRol;
        try {
            nombreRol = NombreRol.valueOf(String.valueOf(datos[4]).trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("El rol del usuario no es válido.");
        }
        Usuario u = new Usuario();
        u.setIdUsuario(((Number) datos[0]).intValue());
        u.setNombre(String.valueOf(datos[1]));
        u.setApellido(String.valueOf(datos[2]));
        u.setNombreUsuario(String.valueOf(datos[3]));
        u.setRol(new Rol(null, nombreRol, null));
        usuario = u;
    }

    public static Usuario getUsuario() {
        if (usuario == null) {
            throw new BusinessException("Debe iniciar sesión para realizar esta operación.");
        }
        return usuario;
    }

    public static boolean haySesion() {
        return usuario != null;
    }

    public static void cerrar() {
        usuario = null;
    }
}
