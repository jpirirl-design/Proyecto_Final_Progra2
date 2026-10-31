package sigelab.controller;

import sigelab.enums.EstadoPrestamo;
import sigelab.model.DetallePrestamo;
import sigelab.model.Prestamo;
import sigelab.seguridad.SesionActual;
import sigelab.service.DevolucionService;
import sigelab.service.ItemDevolucion;

import java.util.List;

public class DevolucionController {

    private final DevolucionService service = new DevolucionService();

    public List<Prestamo> listarPrestamosActivos() {
        return service.listarPrestamosActivos();
    }

    public List<DetallePrestamo> listarPendientes(int idPrestamo) {
        return service.listarPendientes(idPrestamo);
    }

    /** @return ACTIVO si quedan equipos pendientes, FINALIZADO si ya se devolvió todo. */
    public EstadoPrestamo devolver(int idPrestamo, List<ItemDevolucion> items) {
        return service.devolver(idPrestamo, SesionActual.getUsuario(), items);
    }
}
