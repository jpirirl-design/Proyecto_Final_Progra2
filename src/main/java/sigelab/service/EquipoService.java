
package sigelab.service;

public class EquipoService {

    public String validarEquipo(
            String codigo,
            String nombre,
            String tipo,
            String laboratorio,
            String marca,
            String modelo,
            String serie
    ) {

        if (codigo == null || codigo.trim().isEmpty()) {
            return "Debe ingresar el código de inventario.";
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            return "Debe ingresar el nombre del equipo.";
        }

        if (tipo == null || tipo.equals("Seleccione tipo")) {
            return "Debe seleccionar un tipo de equipo.";
        }

        if (laboratorio == null || laboratorio.equals("Seleccione laboratorio")) {
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
}
