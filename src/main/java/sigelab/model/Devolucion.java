package sigelab.model;

import sigelab.enums.CondicionDevolucion;
import java.time.LocalDateTime;

public class Devolucion {

    private Integer idDevolucion;
    private DetallePrestamo detallePrestamo;
    private LocalDateTime fechaDevolucion;
    private CondicionDevolucion condicion;
    private String observacion;
    private Usuario usuarioRegistra;

    public Devolucion() {
    }

    public Devolucion(Integer idDevolucion, DetallePrestamo detallePrestamo, LocalDateTime fechaDevolucion, CondicionDevolucion condicion, String observacion, Usuario usuarioRegistra) {
        this.idDevolucion = idDevolucion;
        this.detallePrestamo = detallePrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.condicion = condicion;
        this.observacion = observacion;
        this.usuarioRegistra = usuarioRegistra;
    }

    public Integer getIdDevolucion() {
        return idDevolucion;
    }

    public void setIdDevolucion(Integer idDevolucion) {
        this.idDevolucion = idDevolucion;
    }

    public DetallePrestamo getDetallePrestamo() {
        return detallePrestamo;
    }

    public void setDetallePrestamo(DetallePrestamo detallePrestamo) {
        this.detallePrestamo = detallePrestamo;
    }

    public LocalDateTime getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDateTime fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public CondicionDevolucion getCondicion() {
        return condicion;
    }

    public void setCondicion(CondicionDevolucion condicion) {
        this.condicion = condicion;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Usuario getUsuarioRegistra() {
        return usuarioRegistra;
    }

    public void setUsuarioRegistra(Usuario usuarioRegistra) {
        this.usuarioRegistra = usuarioRegistra;
    }
}
