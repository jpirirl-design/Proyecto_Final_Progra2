package sigelab.controller;

import sigelab.enums.EstadoEquipo;
import sigelab.enums.ResultadoMantenimiento;
import sigelab.enums.TipoMantenimiento;
import sigelab.model.Equipo;
import sigelab.seguridad.SesionActual;
import sigelab.service.MantenimientoService;
import sigelab.service.QrEquipoService;

import java.util.List;

public class MantenimientoController {

    private final MantenimientoService service = new MantenimientoService();
    private final QrEquipoService equipoService = new QrEquipoService();

    /** Solo identifica el equipo por su código; no cambia nada. */
    public Equipo identificar(String codigoEquipo) {
        return equipoService.identificarPorCodigo(codigoEquipo);
    }

    /** @return el ID del mantenimiento creado. */
    public int enviar(String codigoEquipo, TipoMantenimiento tipo, String descripcion,
                      String responsable, String observaciones) {
        Equipo equipo = equipoService.identificarPorCodigo(codigoEquipo);
        return service.enviarAMantenimiento(equipo.getIdEquipo(), tipo, descripcion,
                responsable, observaciones, SesionActual.getUsuario());
    }

    /** Filas listas para la tabla: {id, código, equipo, tipo, descripción, responsable, ingreso}. */
    public List<Object[]> listarEnProceso() {
        return service.listarEnProceso().stream()
                .map(m -> new Object[]{
                        m.idMantenimiento(), m.codigoEquipo(), m.nombreEquipo(), m.tipo(),
                        m.descripcion(), m.responsable(), m.fechaIngreso()})
                .toList();
    }

    /** @return el nuevo estado del equipo (DISPONIBLE o BAJA). */
    public EstadoEquipo finalizar(int idMantenimiento, ResultadoMantenimiento resultado, String observaciones) {
        return service.finalizarMantenimiento(idMantenimiento, resultado, observaciones, SesionActual.getUsuario());
    }
}
