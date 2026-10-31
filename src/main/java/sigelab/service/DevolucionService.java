package sigelab.service;

import sigelab.seguridad.Autorizacion;
import sigelab.config.ConexionDB;
import sigelab.enums.AccionAuditoria;
import sigelab.enums.CondicionDevolucion;
import sigelab.enums.EstadoDetalle;
import sigelab.enums.EstadoEquipo;
import sigelab.enums.EstadoPrestamo;
import sigelab.exception.BusinessException;
import sigelab.exception.DataAccessException;
import sigelab.model.DetallePrestamo;
import sigelab.model.Prestamo;
import sigelab.model.Usuario;
import sigelab.repository.AuditoriaRepository;
import sigelab.repository.DevolucionRepository;
import sigelab.repository.DevolucionRepository.DetalleDevolucion;
import sigelab.repository.EquipoEstadoRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DevolucionService {

    private static final int MAX_OBSERVACION = 300;

    private final DevolucionRepository devolucionRepository;
    private final EquipoEstadoRepository equipoRepository;
    private final AuditoriaRepository auditoriaRepository;

    public DevolucionService() {
        this(new DevolucionRepository(), new EquipoEstadoRepository(), new AuditoriaRepository());
    }

    public DevolucionService(DevolucionRepository devolucionRepository,
                             EquipoEstadoRepository equipoRepository,
                             AuditoriaRepository auditoriaRepository) {
        this.devolucionRepository = devolucionRepository;
        this.equipoRepository = equipoRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    public List<Prestamo> listarPrestamosActivos() {
        return devolucionRepository.listarPrestamosActivos();
    }

    public List<DetallePrestamo> listarPendientes(int idPrestamo) {
        if (idPrestamo <= 0) {
            throw new BusinessException("Seleccione un préstamo válido.");
        }
        return devolucionRepository.listarPendientes(idPrestamo);
    }

    /**
     * Registra la devolución de uno o varios equipos de un préstamo (parcial o completa).
     * Todo o nada. Reglas: RN-10, RN-11, RN-12, RN-13, RN-16, RN-18, RN-20.
     *
     * @return ACTIVO si todavía quedan equipos pendientes; FINALIZADO si ya no queda ninguno.
     */
    public EstadoPrestamo devolver(int idPrestamo, Usuario usuario, List<ItemDevolucion> items) {

        Autorizacion.exigirOperador(usuario);
        validarDatos(idPrestamo, usuario, items);

        try (Connection con = ConexionDB.conectar()) {
            con.setAutoCommit(false);
            try {
                for (ItemDevolucion item : items) {

                    DetalleDevolucion detalle = devolucionRepository
                            .buscarDetalleParaDevolver(con, item.idDetalle())
                            .orElseThrow(() -> new BusinessException("El equipo indicado no existe en ningún préstamo."));

                    if (detalle.idPrestamo() != idPrestamo) {                       // RN-10
                        throw new BusinessException("El equipo " + detalle.codigoEquipo()
                                + " no pertenece al préstamo seleccionado.");
                    }
                    if (detalle.estadoPrestamo() != EstadoPrestamo.ACTIVO) {        // RN-10
                        throw new BusinessException("El préstamo seleccionado ya está finalizado.");
                    }
                    if (detalle.estadoDetalle() == EstadoDetalle.DEVUELTO) {        // RN-11
                        throw new BusinessException("El equipo " + detalle.codigoEquipo() + " ya fue devuelto.");
                    }

                    devolucionRepository.guardarDevolucion(con, item.idDetalle(), item.condicion(),
                            limpiar(item.observacion()), usuario.getIdUsuario());
                    devolucionRepository.marcarDetalleDevuelto(con, item.idDetalle());

                    EstadoEquipo nuevoEstado = item.condicion() == CondicionDevolucion.BUENO
                            ? EstadoEquipo.DISPONIBLE                                // RN-12
                            : EstadoEquipo.DANADO;                                   // RN-13
                    equipoRepository.actualizarEstado(con, detalle.idEquipo(), nuevoEstado);

                    auditoriaRepository.registrar(con, usuario.getIdUsuario(),
                            AccionAuditoria.EQUIPO_DEVUELTO.name(), "DetallePrestamo", item.idDetalle(),
                            "Equipo " + detalle.codigoEquipo() + " devuelto en condición " + item.condicion()
                                    + " (préstamo #" + idPrestamo + ").");
                }

                // RN-16: el préstamo sigue activo mientras quede algún equipo pendiente
                EstadoPrestamo resultado = EstadoPrestamo.ACTIVO;
                if (devolucionRepository.contarPendientes(con, idPrestamo) == 0) {
                    devolucionRepository.finalizarPrestamo(con, idPrestamo);
                    resultado = EstadoPrestamo.FINALIZADO;
                }

                con.commit();
                return resultado;

            } catch (BusinessException | DataAccessException e) {
                rollbackSilencioso(con);
                throw e;
            } catch (RuntimeException e) {
                rollbackSilencioso(con);
                throw new DataAccessException("No se pudo registrar la devolución.", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo registrar la devolución.", e);
        }
    }

    private void validarDatos(int idPrestamo, Usuario usuario, List<ItemDevolucion> items) {
        if (idPrestamo <= 0) {
            throw new BusinessException("Seleccione un préstamo válido.");
        }
        if (usuario == null || usuario.getIdUsuario() == null) {
            throw new BusinessException("No se identificó al usuario que registra la devolución.");
        }
        if (items == null || items.isEmpty()) {
            throw new BusinessException("Seleccione al menos un equipo para devolver.");
        }
        Set<Integer> vistos = new HashSet<>();
        for (ItemDevolucion item : items) {
            if (item == null || item.idDetalle() <= 0) {
                throw new BusinessException("Hay un equipo inválido en la lista.");
            }
            if (item.condicion() == null) {
                throw new BusinessException("Indique la condición (BUENO o DANADO) de cada equipo.");
            }
            if (!vistos.add(item.idDetalle())) {
                throw new BusinessException("El mismo equipo no puede devolverse dos veces en una operación.");
            }
            if (item.observacion() != null && item.observacion().trim().length() > MAX_OBSERVACION) {
                throw new BusinessException("La observación no puede superar " + MAX_OBSERVACION + " caracteres.");
            }
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