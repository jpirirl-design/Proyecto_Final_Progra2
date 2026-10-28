
package sigelab.service;

public class LaboratorioService {

    public String validarLaboratorio(
            String codigo,
            String nombre,
            String ubicacion,
            int capacidad
    ) {

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
}
