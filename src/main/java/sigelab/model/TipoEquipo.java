package sigelab.model;

public class TipoEquipo {

    private Integer idTipoEquipo;
    private String nombre;
    private String descripcion;

    public TipoEquipo() {
    }

    public TipoEquipo(Integer idTipoEquipo, String nombre, String descripcion) {
        this.idTipoEquipo = idTipoEquipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Integer getIdTipoEquipo() {
        return idTipoEquipo;
    }

    public void setIdTipoEquipo(Integer idTipoEquipo) {
        this.idTipoEquipo = idTipoEquipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
