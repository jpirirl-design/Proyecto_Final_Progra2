package sigelab.model;

/** Cantidades que muestra el formulario principal (sección 6.11). */
public record ResumenDashboard(
        int totalEquipos,
        int disponibles,
        int prestados,
        int danados,
        int enMantenimiento,
        int deBaja,
        int prestamosActivos,
        int prestamosVencidos) {
}
