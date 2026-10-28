
package sigelab.controller;

import sigelab.service.EquipoService;

public class EquipoController {

    private final EquipoService service;

    public EquipoController() {
        service = new EquipoService();
    }

    public String validarEquipo(
            String codigo,
            String nombre,
            String tipo,
            String laboratorio,
            String marca,
            String modelo,
            String serie
    ) {
        return service.validarEquipo(
                codigo,
                nombre,
                tipo,
                laboratorio,
                marca,
                modelo,
                serie
        );
    }
}
