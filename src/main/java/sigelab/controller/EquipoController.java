
package sigelab.controller;

import sigelab.service.EquipoService;

import java.sql.SQLException;
import java.util.List;

public class EquipoController {

    private final EquipoService service;

    public EquipoController() {
        service = new EquipoService();
    }

    // VALIDAR DATOS DEL EQUIPO
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

    // CONSULTAR TODOS LOS EQUIPOS
    public List<Object[]> listar() throws SQLException {
        return service.listar();
    }

    // CARGAR TIPOS DE EQUIPO Y LABORATORIOS
    public List<Object[]> listarOpciones(String tabla)
            throws SQLException {
        return service.listarOpciones(tabla);
    }

    // GUARDAR EQUIPO
    public void guardarEquipo(
            String codigo,
            String nombre,
            int tipo,
            int laboratorio,
            String marca,
            String modelo,
            String serie,
            String estado,
            String observaciones
    ) throws SQLException {

        service.guardarEquipo(
                codigo,
                nombre,
                tipo,
                laboratorio,
                marca,
                modelo,
                serie,
                estado,
                observaciones
        );
    }

    // EDITAR EQUIPO
    public void editarEquipo(
            int id,
            String codigo,
            String nombre,
            int tipo,
            int laboratorio,
            String marca,
            String modelo,
            String serie,
            String estado,
            String observaciones
    ) throws SQLException {

        service.editarEquipo(
                id,
                codigo,
                nombre,
                tipo,
                laboratorio,
                marca,
                modelo,
                serie,
                estado,
                observaciones
        );
    }

    // DAR DE BAJA UN EQUIPO
    public void darDeBaja(int id) throws SQLException {
        service.darDeBaja(id);
    }
}
