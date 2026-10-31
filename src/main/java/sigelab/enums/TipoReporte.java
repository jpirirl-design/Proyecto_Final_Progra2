package sigelab.enums;

public enum TipoReporte {
    RESUMEN_EQUIPOS_POR_ESTADO("Cantidad de equipos por estado"),
    EQUIPOS_POR_ESTADO("Equipos por estado"),
    PRESTAMOS_ACTIVOS("Préstamos activos"),
    PRESTAMOS_FINALIZADOS("Préstamos finalizados"),
    PRESTAMOS_VENCIDOS("Préstamos vencidos"),
    PRESTAMOS_ENTRE_FECHAS("Préstamos entre fechas"),
    HISTORIAL_EQUIPO("Historial de préstamos de un equipo"),
    HISTORIAL_MANTENIMIENTO("Historial de mantenimiento"),
    AUDITORIA("Auditoría (solo ADMIN)");

    private final String descripcion;

    TipoReporte(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean usaEstado() {
        return this == EQUIPOS_POR_ESTADO;
    }

    public boolean usaFechas() {
        return this == PRESTAMOS_ENTRE_FECHAS || this == AUDITORIA;
    }

    public boolean usaCodigo() {
        return this == HISTORIAL_EQUIPO || this == HISTORIAL_MANTENIMIENTO;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
