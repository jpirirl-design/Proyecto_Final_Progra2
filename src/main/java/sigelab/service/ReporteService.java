package sigelab.service;

import sigelab.enums.TipoReporte;
import sigelab.exception.BusinessException;
import sigelab.model.FiltroReporte;
import sigelab.model.ResultadoConsulta;
import sigelab.model.ResumenDashboard;
import sigelab.model.Usuario;
import sigelab.repository.ReporteRepository;
import sigelab.seguridad.Autorizacion;

public class ReporteService {

    private final ReporteRepository repository;

    public ReporteService() {
        this(new ReporteRepository());
    }

    public ReporteService(ReporteRepository repository) {
        this.repository = repository;
    }

    public ResumenDashboard obtenerDashboard() {
        return repository.obtenerResumen();
    }

    /** Valida los filtros que necesita cada reporte y el permiso del usuario. */
    public ResultadoConsulta generar(TipoReporte tipo, FiltroReporte filtro, Usuario usuario) {
        if (tipo == null) {
            throw new BusinessException("Seleccione un reporte.");
        }
        if (filtro == null) {
            filtro = new FiltroReporte(null, null, null, null);
        }

        if (tipo.usaFechas()) {
            if (filtro.desde() == null || filtro.hasta() == null) {
                throw new BusinessException("Indique la fecha inicial y la fecha final.");
            }
            if (filtro.desde().isAfter(filtro.hasta())) {
                throw new BusinessException("La fecha inicial no puede ser posterior a la fecha final.");
            }
        }

        String codigo = filtro.codigoEquipo() == null || filtro.codigoEquipo().isBlank()
                ? null : filtro.codigoEquipo().trim();

        return switch (tipo) {
            case RESUMEN_EQUIPOS_POR_ESTADO -> repository.resumenEquiposPorEstado();
            case EQUIPOS_POR_ESTADO -> repository.equiposPorEstado(filtro.estado());
            case PRESTAMOS_ACTIVOS -> repository.prestamosPorEstado("ACTIVO");
            case PRESTAMOS_FINALIZADOS -> repository.prestamosPorEstado("FINALIZADO");
            case PRESTAMOS_VENCIDOS -> repository.prestamosVencidos();
            case PRESTAMOS_ENTRE_FECHAS -> repository.prestamosEntreFechas(filtro.desde(), filtro.hasta());
            case HISTORIAL_EQUIPO -> {
                if (codigo == null) {
                    throw new BusinessException("Ingrese el código del equipo.");
                }
                yield repository.historialEquipo(codigo);
            }
            case HISTORIAL_MANTENIMIENTO -> repository.historialMantenimiento(codigo);
            case AUDITORIA -> {
                Autorizacion.exigirAdmin(usuario);
                yield repository.auditoria(filtro.desde(), filtro.hasta());
            }
        };
    }
}
