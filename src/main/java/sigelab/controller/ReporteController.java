package sigelab.controller;

import sigelab.enums.TipoReporte;
import sigelab.model.FiltroReporte;
import sigelab.model.ResultadoConsulta;
import sigelab.model.ResumenDashboard;
import sigelab.seguridad.SesionActual;
import sigelab.service.ReporteService;

public class ReporteController {

    private final ReporteService service = new ReporteService();

    public ResumenDashboard obtenerDashboard() {
        return service.obtenerDashboard();
    }

    public ResultadoConsulta generar(TipoReporte tipo, FiltroReporte filtro) {
        return service.generar(tipo, filtro, SesionActual.getUsuario());
    }
}
