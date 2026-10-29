
package sigelab.service;

import sigelab.repository.EquipoRepository;

import java.sql.SQLException;
import java.util.List;

public class EquipoService {

    private final EquipoRepository repository =
            new EquipoRepository();

    // VALIDACIONES
    public String validarEquipo(
            String codigo,
            String nombre,
            String tipo,
            String laboratorio,
            String marca,
            String modelo,
            String serie) {

        if (codigo == null || codigo.trim().isEmpty()) {
            return "Debe ingresar el código de inventario.";
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            return "Debe ingresar el nombre del equipo.";
        }

        if (tipo == null || tipo.trim().isEmpty()
                || tipo.equals("Seleccione tipo")
                || tipo.equals("-1")) {
            return "Debe seleccionar un tipo de equipo.";
        }

        if (laboratorio == null || laboratorio.trim().isEmpty()
                || laboratorio.equals("Seleccione laboratorio")
                || laboratorio.equals("-1")) {
            return "Debe seleccionar un laboratorio.";
        }

        if (marca == null || marca.trim().isEmpty()) {
            return "Debe ingresar la marca del equipo.";
        }

        if (modelo == null || modelo.trim().isEmpty()) {
            return "Debe ingresar el modelo del equipo.";
        }

        if (serie == null || serie.trim().isEmpty()) {
            return "Debe ingresar el número de serie.";
        }

        return null;
    }

    // CONSULTAR EQUIPOS
    public List<Object[]> listar() throws SQLException {
        return repository.listar();
    }

    // CARGAR TIPOS DE EQUIPO Y LABORATORIOS
    public List<Object[]> listarOpciones(String tabla)
            throws SQLException {
        return repository.listarOpciones(tabla);
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
            String observaciones) throws SQLException {

        String error = validarEquipo(
                codigo,
                nombre,
                String.valueOf(tipo),
                String.valueOf(laboratorio),
                marca,
                modelo,
                serie
        );

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        validarEstado(estado);

        repository.guardar(
                codigo.trim(),
                nombre.trim(),
                tipo,
                laboratorio,
                marca.trim(),
                modelo.trim(),
                serie.trim(),
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
            String observaciones) throws SQLException {

        String error = validarEquipo(
                codigo,
                nombre,
                String.valueOf(tipo),
                String.valueOf(laboratorio),
                marca,
                modelo,
                serie
        );

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        validarEstado(estado);

        repository.editar(
                id,
                codigo.trim(),
                nombre.trim(),
                tipo,
                laboratorio,
                marca.trim(),
                modelo.trim(),
                serie.trim(),
                estado,
                observaciones
        );
    }

    // DAR DE BAJA
    public void darDeBaja(int id) throws SQLException {
        repository.darDeBaja(id);
    }

    // VALIDAR ESTADO
    private void validarEstado(String estado) {

        if (!List.of(
                "DISPONIBLE",
                "PRESTADO",
                "DAÑADO",
                "MANTENIMIENTO",
                "BAJA"
        ).contains(estado)) {

            throw new IllegalArgumentException(
                    "El estado del equipo no es válido."
            );
        }
    }
}
