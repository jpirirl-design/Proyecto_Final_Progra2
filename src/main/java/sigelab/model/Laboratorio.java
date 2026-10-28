package sigelab.model;

import sigelab.enums.EstadoLaboratorio;

public class Laboratorio {

    private Integer idLaboratorio;
    private String codigo;
    private String nombre;
    private String ubicacion;
    private Integer capacidad;
    private EstadoLaboratorio estado;

    public Laboratorio() {
    }

    public Laboratorio(Integer idLaboratorio, String codigo, String nombre, String ubicacion, Integer capacidad, EstadoLaboratorio estado) {
        this.idLaboratorio = idLaboratorio;
        this.codigo = codigo;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    public Integer getIdLaboratorio() {
        return idLaboratorio;
    }

    public void setIdLaboratorio(Integer idLaboratorio) {
        this.idLaboratorio = idLaboratorio;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public EstadoLaboratorio getEstado() {
        return estado;
    }

    public void setEstado(EstadoLaboratorio estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
