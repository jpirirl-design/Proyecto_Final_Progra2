package sigelab.model;

import sigelab.enums.EstadoPrestamo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Prestamo {

    private Integer idPrestamo;
    private String solicitante;
    private LocalDateTime fechaPrestamo;
    private LocalDate fechaEsperadaDevolucion;
    private Usuario usuarioRegistra;
    private EstadoPrestamo estado;
    private String observaciones;
    private List<DetallePrestamo> detalles = new ArrayList<>();

    public Prestamo() {
    }

    public Prestamo(Integer idPrestamo, String solicitante, LocalDateTime fechaPrestamo, LocalDate fechaEsperadaDevolucion, Usuario usuarioRegistra, EstadoPrestamo estado, String observaciones) {
        this.idPrestamo = idPrestamo;
        this.solicitante = solicitante;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaEsperadaDevolucion = fechaEsperadaDevolucion;
        this.usuarioRegistra = usuarioRegistra;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    public Integer getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(Integer idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }

    public LocalDateTime getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDateTime fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaEsperadaDevolucion() {
        return fechaEsperadaDevolucion;
    }

    public void setFechaEsperadaDevolucion(LocalDate fechaEsperadaDevolucion) {
        this.fechaEsperadaDevolucion = fechaEsperadaDevolucion;
    }

    public Usuario getUsuarioRegistra() {
        return usuarioRegistra;
    }

    public void setUsuarioRegistra(Usuario usuarioRegistra) {
        this.usuarioRegistra = usuarioRegistra;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<DetallePrestamo> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePrestamo> detalles) {
        this.detalles = detalles;
    }

    public void agregarDetalle(DetallePrestamo detalle) {
        detalle.setPrestamo(this);
        this.detalles.add(detalle);
    }
}
