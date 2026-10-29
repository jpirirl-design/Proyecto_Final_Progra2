
package sigelab.controller;

import sigelab.service.TipoEquipoService;

import java.sql.SQLException;
import java.util.List;

public class TipoEquipoController {

    private final TipoEquipoService service =
            new TipoEquipoService();

    public String validarTipoEquipo(
            String nombre, String descripcion) {

        return service.validarTipoEquipo(nombre, descripcion);
    }

    public void guardar(String nombre, String descripcion)
            throws SQLException {
        service.guardar(nombre, descripcion);
    }

    public List<Object[]> listar() throws SQLException {
        return service.listar();
    }

    public void editar(int id, String nombre, String descripcion)
            throws SQLException {
        service.editar(id, nombre, descripcion);
    }

    public void eliminar(int id) throws SQLException {
        service.eliminar(id);
    }
}
