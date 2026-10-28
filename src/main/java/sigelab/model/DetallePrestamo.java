package sigelab.model;

import sigelab.enums.EstadoDetalle;

public class DetallePrestamo {

    private Integer idDetalle;
    private Prestamo prestamo;
    private Equipo equipo;
    private EstadoDetalle estado;

    public DetallePrestamo() {
    }

    public DetallePrestamo(Integer idDetalle, Prestamo prestamo, Equipo equipo, EstadoDetalle estado) {
        this.idDetalle = idDetalle;
        this.prestamo = prestamo;
        this.equipo = equipo;
        this.estado = estado;
    }

    public Integer getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public EstadoDetalle getEstado() {
        return estado;
    }

    public void setEstado(EstadoDetalle estado) {
        this.estado = estado;
    }
}
