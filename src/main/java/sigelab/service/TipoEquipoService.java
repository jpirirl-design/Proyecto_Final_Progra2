
package sigelab.service;

public class TipoEquipoService {

    public String validarTipoEquipo(String nombre, String descripcion) {

        if (nombre == null || nombre.trim().isEmpty()) {
            return "Debe ingresar el nombre del tipo de equipo.";
        }

        if (descripcion == null || descripcion.trim().isEmpty()) {
            return "Debe ingresar una descripción.";
        }

        return null;
    }
}
