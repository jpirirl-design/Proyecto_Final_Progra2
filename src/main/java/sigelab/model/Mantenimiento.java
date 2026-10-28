package sigelab.model;

import sigelab.enums.EstadoMantenimiento;
import sigelab.enums.ResultadoMantenimiento;
import sigelab.enums.TipoMantenimiento;
import java.time.LocalDate;

public class Mantenimiento {

    private Integer idMantenimiento;
    private Equipo equipo;
    private LocalDate fechaIngreso;
    private TipoMantenimiento tipo;
    private String descripcion;
    private String responsable;
    private LocalDate fechaSalida;
    private ResultadoMantenimiento resultado;
    private EstadoMantenimiento estado;
    private String observaciones;

    public Mantenimiento() {
    }

    public Mantenimiento(Integer idMantenimiento, Equipo equipo, LocalDate fechaIngreso, TipoMantenimiento tipo, String descripcion, String responsable, LocalDate fechaSalida, ResultadoMantenimiento resultado, EstadoMantenimiento estado, String observaciones) {
        this.idMantenimiento = idMantenimiento;
        this.equipo = equipo;
        this.fechaIngreso = fechaIngreso;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.responsable = responsable;
        this.fechaSalida = fechaSalida;
        this.resultado = resultado;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    public Integer getIdMantenimiento() {
        return idMantenimiento;
    }

    public void setIdMantenimiento(Integer idMantenimiento) {
        this.idMantenimiento = idMantenimiento;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public TipoMantenimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMantenimiento tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public ResultadoMantenimiento getResultado() {
        return resultado;
    }

    public void setResultado(ResultadoMantenimiento resultado) {
        this.resultado = resultado;
    }

    public EstadoMantenimiento getEstado() {
        return estado;
    }

    public void setEstado(EstadoMantenimiento estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
