
package sigelab.controller;

import sigelab.service.LaboratorioService;

public class LaboratorioController {

    private final LaboratorioService service;

    public LaboratorioController() {
        service = new LaboratorioService();
    }

    public String validarLaboratorio(
            String codigo,
            String nombre,
            String ubicacion,
            int capacidad
    ) {
        return service.validarLaboratorio(
                codigo,
                nombre,
                ubicacion,
                capacidad
        );
    }
}
