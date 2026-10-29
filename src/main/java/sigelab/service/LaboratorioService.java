
package sigelab.service;

import sigelab.repository.LaboratorioRepository;

import java.sql.SQLException;
import java.util.List;

public class LaboratorioService {

    private final LaboratorioRepository repository =
            new LaboratorioRepository();

    public String validarLaboratorio(
            String codigo, String nombre,
            String ubicacion, int capacidad) {

        if (codigo == null || codigo.trim().isEmpty()) {
            return "Debe ingresar el código del laboratorio.";
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            return "Debe ingresar el nombre del laboratorio.";
        }
        if (ubicacion == null || ubicacion.trim().isEmpty()) {
            return "Debe ingresar la ubicación del laboratorio.";
        }
        if (capacidad <= 0) {
            return "La capacidad debe ser mayor que cero.";
        }
        return null;
    }

    public void guardar(String codigo, String nombre,
                        String ubicacion, int capacidad)
            throws SQLException {

        String error = validarLaboratorio(
                codigo, nombre, ubicacion, capacidad);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        repository.guardar(codigo.trim(), nombre.trim(),
                ubicacion.trim(), capacidad);
    }

    public List<Object[]> listar() throws SQLException {
        return repository.listar();
    }

    public void editar(int id, String codigo, String nombre,
                       String ubicacion, int capacidad, String estado)
            throws SQLException {

        String error = validarLaboratorio(
                codigo, nombre, ubicacion, capacidad);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        if (!"ACTIVO".equals(estado) &&
                !"INACTIVO".equals(estado)) {
            throw new IllegalArgumentException("Estado no válido.");
        }

        repository.editar(id, codigo.trim(), nombre.trim(),
                ubicacion.trim(), capacidad, estado);
    }

    public void desactivar(int id) throws SQLException {
        repository.desactivar(id);
    }
}
