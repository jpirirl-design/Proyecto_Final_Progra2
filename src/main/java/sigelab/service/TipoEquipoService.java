
package sigelab.service;

import sigelab.repository.TipoEquipoRepository;

import java.sql.SQLException;
import java.util.List;

public class TipoEquipoService {

    private final TipoEquipoRepository repository =
            new TipoEquipoRepository();

    public String validarTipoEquipo(
            String nombre, String descripcion) {

        if (nombre == null || nombre.trim().isEmpty()) {
            return "Debe ingresar el nombre del tipo de equipo.";
        }

        if (descripcion == null || descripcion.trim().isEmpty()) {
            return "Debe ingresar una descripción.";
        }

        return null;
    }

    public void guardar(String nombre, String descripcion)
            throws SQLException {

        String error = validarTipoEquipo(nombre, descripcion);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        repository.guardar(nombre.trim(), descripcion.trim());
    }

    public List<Object[]> listar() throws SQLException {
        return repository.listar();
    }

    public void editar(int id, String nombre, String descripcion)
            throws SQLException {

        String error = validarTipoEquipo(nombre, descripcion);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        repository.editar(id, nombre.trim(), descripcion.trim());
    }

    public void eliminar(int id) throws SQLException {
        repository.eliminar(id);
    }
}
