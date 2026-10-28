
package sigelab.controller;

import sigelab.service.TipoEquipoService;

public class TipoEquipoController {

    private final TipoEquipoService service;

    public TipoEquipoController() {
        service = new TipoEquipoService();
    }

    public String validarTipoEquipo(String nombre, String descripcion) {
        return service.validarTipoEquipo(nombre, descripcion);
    }
}
