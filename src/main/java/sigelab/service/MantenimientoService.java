package sigelab.service;

import sigelab.seguridad.Autorizacion;
import sigelab.config.ConexionDB;
import sigelab.enums.AccionAuditoria;
import sigelab.enums.EstadoEquipo;
import sigelab.enums.EstadoMantenimiento;
import sigelab.enums.ResultadoMantenimiento;
import sigelab.enums.TipoMantenimiento;
import sigelab.exception.BusinessException;
import sigelab.exception.DataAccessException;
import sigelab.model.Equipo;
import sigelab.model.Usuario;
import sigelab.repository.AuditoriaRepository;
import sigelab.repository.EquipoEstadoRepository;
import sigelab.repository.MantenimientoRepository;
import sigelab.repository.MantenimientoRepository.MantenimientoBloqueado;
import sigelab.repository.MantenimientoRepository.RegistroMantenimiento;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MantenimientoService {

    private static final int MAX_DESCRIPCION = 300;
    private static final int MAX_RESPONSABLE = 150;
    private static final int MAX_OBSERVACIONES = 300;

    private final MantenimientoRepository mantenimientoRepository;
    private final EquipoEstadoRepository equipoRepository;
    private final AuditoriaRepository auditoriaRepository;

    public MantenimientoService() {
        this(new MantenimientoRepository(), new EquipoEstadoRepository(), new AuditoriaRepository());
    }

    public MantenimientoService(MantenimientoRepository mantenimientoRepository,
                                EquipoEstadoRepository equipoRepository,
                                AuditoriaRepository auditoriaRepository) {
        this.mantenimientoRepository = mantenimientoRepository;
        this.equipoRepository = equipoRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    public List<RegistroMantenimiento> listarEnProceso() {
        return mantenimientoRepository.listarEnProceso();
    }

    /**
     * DANADO -> MANTENIMIENTO, o DISPONIBLE -> MANTENIMIENTO (solo preventivo).
     * Cualquier otro estado (PRESTADO, MANTENIMIENTO, BAJA) se rechaza.
     *
     * @return el ID del mantenimiento creado.
     */
    public int enviarAMantenimiento(int idEquipo, TipoMantenimiento tipo, String descripcion,
                                    String responsable, String observaciones, Usuario usuario) {

        Autorizacion.exigirOperador(usuario);
        validarEnvio(idEquipo, tipo, descripcion, responsable, observaciones, usuario);

        try (Connection con = ConexionDB.conectar()) {
            con.setAutoCommit(false);
            try {
                Equipo equipo = equipoRepository.buscarPorIdParaActualizar(con, idEquipo)
                        .orElseThrow(() -> new BusinessException("El equipo seleccionado no existe."));

                switch (equipo.getEstado()) {
                    case DANADO -> { /* permitido */ }
                    case DISPONIBLE -> {
                        if (tipo != TipoMantenimiento.PREVENTIVO) {
                            throw new BusinessException("Un equipo disponible solo puede enviarse "
                                    + "a mantenimiento preventivo.");
                        }
                    }
                    default -> throw new BusinessException("El equipo " + equipo.getCodigoInventario()
                            + " no puede enviarse a mantenimiento porque está " + equipo.getEstado() + ".");
                }

                int idMantenimiento = mantenimientoRepository.guardar(con, idEquipo, LocalDate.now(), tipo,
                        descripcion.trim(), responsable.trim(), limpiar(observaciones));

                equipoRepository.actualizarEstado(con, idEquipo, EstadoEquipo.MANTENIMIENTO);

                auditoriaRepository.registrar(con, usuario.getIdUsuario(),
                        AccionAuditoria.EQUIPO_ENVIADO_MANTENIMIENTO.name(), "Mantenimiento", idMantenimiento,
                        "Equipo " + equipo.getCodigoInventario() + " enviado a mantenimiento " + tipo
                                + " (estado previo: " + equipo.getEstado() + ").");

                con.commit();
                return idMantenimiento;

            } catch (BusinessException | DataAccessException e) {
                rollbackSilencioso(con);
                throw e;
            } catch (RuntimeException e) {
                rollbackSilencioso(con);
                throw new DataAccessException("No se pudo enviar el equipo a mantenimiento.", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo enviar el equipo a mantenimiento.", e);
        }
    }

    /**
     * MANTENIMIENTO -> DISPONIBLE (REPARADO) o MANTENIMIENTO -> BAJA (NO_REPARABLE). RN-15.
     *
     * @return el nuevo estado del equipo.
     */
    public EstadoEquipo finalizarMantenimiento(int idMantenimiento, ResultadoMantenimiento resultado,
                                               String observaciones, Usuario usuario) {

        Autorizacion.exigirOperador(usuario);
        validarCierre(idMantenimiento, resultado, observaciones, usuario);

        try (Connection con = ConexionDB.conectar()) {
            con.setAutoCommit(false);
            try {
                MantenimientoBloqueado mantenimiento = mantenimientoRepository
                        .buscarParaFinalizar(con, idMantenimiento)
                        .orElseThrow(() -> new BusinessException("El mantenimiento seleccionado no existe."));

                if (mantenimiento.estado() == EstadoMantenimiento.FINALIZADO) {
                    throw new BusinessException("El mantenimiento del equipo "
                            + mantenimiento.codigoEquipo() + " ya fue finalizado.");
                }

                Equipo equipo = equipoRepository.buscarPorIdParaActualizar(con, mantenimiento.idEquipo())
                        .orElseThrow(() -> new BusinessException("El equipo del mantenimiento no existe."));
                if (equipo.getEstado() != EstadoEquipo.MANTENIMIENTO) {
                    throw new BusinessException("El equipo " + equipo.getCodigoInventario()
                            + " no está en mantenimiento (estado actual: " + equipo.getEstado() + ").");
                }

                mantenimientoRepository.finalizar(con, idMantenimiento, LocalDate.now(),
                        resultado, limpiar(observaciones));

                EstadoEquipo nuevoEstado = resultado == ResultadoMantenimiento.REPARADO
                        ? EstadoEquipo.DISPONIBLE
                        : EstadoEquipo.BAJA;
                equipoRepository.actualizarEstado(con, equipo.getIdEquipo(), nuevoEstado);

                auditoriaRepository.registrar(con, usuario.getIdUsuario(),
                        AccionAuditoria.MANTENIMIENTO_FINALIZADO.name(), "Mantenimiento", idMantenimiento,
                        "Mantenimiento de " + equipo.getCodigoInventario() + " finalizado como "
                                + resultado + "; el equipo pasa a " + nuevoEstado + ".");

                con.commit();
                return nuevoEstado;

            } catch (BusinessException | DataAccessException e) {
                rollbackSilencioso(con);
                throw e;
            } catch (RuntimeException e) {
                rollbackSilencioso(con);
                throw new DataAccessException("No se pudo finalizar el mantenimiento.", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo finalizar el mantenimiento.", e);
        }
    }

    private void validarEnvio(int idEquipo, TipoMantenimiento tipo, String descripcion,
                              String responsable, String observaciones, Usuario usuario) {
        if (idEquipo <= 0) {
            throw new BusinessException("Seleccione un equipo válido.");
        }
        if (tipo == null) {
            throw new BusinessException("Seleccione el tipo de mantenimiento.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new BusinessException("La descripción del mantenimiento es obligatoria.");
        }
        if (descripcion.trim().length() > MAX_DESCRIPCION) {
            throw new BusinessException("La descripción no puede superar " + MAX_DESCRIPCION + " caracteres.");
        }
        if (responsable == null || responsable.isBlank()) {
            throw new BusinessException("El responsable del mantenimiento es obligatorio.");
        }
        if (responsable.trim().length() > MAX_RESPONSABLE) {
            throw new BusinessException("El responsable no puede superar " + MAX_RESPONSABLE + " caracteres.");
        }
        validarObservaciones(observaciones);
        validarUsuario(usuario);
    }

    private void validarCierre(int idMantenimiento, ResultadoMantenimiento resultado,
                               String observaciones, Usuario usuario) {
        if (idMantenimiento <= 0) {
            throw new BusinessException("Seleccione un mantenimiento válido.");
        }
        if (resultado == null) {
            throw new BusinessException("Indique el resultado: REPARADO o NO_REPARABLE.");
        }
        validarObservaciones(observaciones);
        validarUsuario(usuario);
    }

    private void validarObservaciones(String observaciones) {
        if (observaciones != null && observaciones.trim().length() > MAX_OBSERVACIONES) {
            throw new BusinessException("Las observaciones no pueden superar " + MAX_OBSERVACIONES + " caracteres.");
        }
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getIdUsuario() == null) {
            throw new BusinessException("No se identificó al usuario que realiza la operación.");
        }
    }

    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private void rollbackSilencioso(Connection con) {
        try {
            con.rollback();
        } catch (SQLException ignorada) {
            // Al cerrar la conexión, SQL Server descarta la transacción pendiente.
        }
    }
}
