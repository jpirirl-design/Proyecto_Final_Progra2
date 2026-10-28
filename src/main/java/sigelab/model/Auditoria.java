package sigelab.model;

import sigelab.enums.AccionAuditoria;
import java.time.LocalDateTime;

public class Auditoria {

    private Long idAuditoria;
    private Usuario usuario;
    private LocalDateTime fechaHora;
    private AccionAuditoria accion;
    private String entidad;
    private Integer idRegistro;
    private String descripcion;

    public Auditoria() {
    }

    public Auditoria(Long idAuditoria, Usuario usuario, LocalDateTime fechaHora, AccionAuditoria accion, String entidad, Integer idRegistro, String descripcion) {
        this.idAuditoria = idAuditoria;
        this.usuario = usuario;
        this.fechaHora = fechaHora;
        this.accion = accion;
        this.entidad = entidad;
        this.idRegistro = idRegistro;
        this.descripcion = descripcion;
    }

    public Long getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Long idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public AccionAuditoria getAccion() {
        return accion;
    }

    public void setAccion(AccionAuditoria accion) {
        this.accion = accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public Integer getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Integer idRegistro) {
        this.idRegistro = idRegistro;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
