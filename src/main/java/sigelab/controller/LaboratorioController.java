
package sigelab.controller;

import sigelab.service.LaboratorioService;

import java.sql.SQLException;
import java.util.List;

public class LaboratorioController {

    private final LaboratorioService service =
            new LaboratorioService();

    public String validarLaboratorio(
            String codigo, String nombre,
            String ubicacion, int capacidad) {

        return service.validarLaboratorio(
                codigo, nombre, ubicacion, capacidad);
    }

    public void guardar(String codigo, String nombre,
                        String ubicacion, int capacidad)
            throws SQLException {

        service.guardar(codigo, nombre, ubicacion, capacidad);
    }

    public List<Object[]> listar() throws SQLException {
        return service.listar();
    }

    public void editar(int id, String codigo, String nombre,
                       String ubicacion, int capacidad, String estado)
            throws SQLException {

        service.editar(id, codigo, nombre, ubicacion,
                capacidad, estado);
    }

    public void desactivar(int id) throws SQLException {
        service.desactivar(id);
    }
}
