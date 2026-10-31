package sigelab.seguridad;

import sigelab.enums.NombreRol;
import sigelab.exception.BusinessException;
import sigelab.model.Usuario;

/**
 * Validación de permisos en la lógica de negocio (RN-20).
 * Ocultar un botón en la pantalla NO reemplaza esta comprobación.
 */
public final class Autorizacion {

    private Autorizacion() {
    }

    /** Operación cotidiana de préstamos, devoluciones y mantenimiento: ADMIN y ENCARGADO. */
    public static void exigirOperador(Usuario usuario) {
        if (usuario == null || usuario.getRol() == null || usuario.getRol().getNombre() == null) {
            throw new BusinessException("No se pudo verificar el rol del usuario.");
        }
        NombreRol rol = usuario.getRol().getNombre();
        if (rol != NombreRol.ADMIN && rol != NombreRol.ENCARGADO) {
            throw new BusinessException("Su rol no tiene permiso para realizar esta operación.");
        }
    }
}
