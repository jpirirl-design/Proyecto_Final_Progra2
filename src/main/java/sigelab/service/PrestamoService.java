package sigelab.service;

import sigelab.config.ConexionDB;
import sigelab.enums.AccionAuditoria;
import sigelab.enums.EstadoDetalle;
import sigelab.enums.EstadoEquipo;
import sigelab.enums.EstadoPrestamo;
import sigelab.exception.BusinessException;
import sigelab.exception.DataAccessException;
import sigelab.model.DetallePrestamo;
import sigelab.model.Equipo;
import sigelab.model.Prestamo;
import sigelab.model.Usuario;
import sigelab.repository.AuditoriaRepository;
import sigelab.repository.EquipoEstadoRepository;
import sigelab.repository.PrestamoRepository;
import sigelab.seguridad.Autorizacion;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final EquipoEstadoRepository equipoRepository;
    private final AuditoriaRepository auditoriaRepository;

    public PrestamoService() {
        this(new PrestamoRepository(), new EquipoEstadoRepository(), new AuditoriaRepository());
    }

    public PrestamoService(PrestamoRepository prestamoRepository,
                           EquipoEstadoRepository equipoRepository,
                           AuditoriaRepository auditoriaRepository) {
        this.prestamoRepository = prestamoRepository;
        this.equipoRepository = equipoRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    /**
     * Operación integral (todo o nada): encabezado + detalle + estados de equipos + auditoría.
     * Reglas: RN-05, RN-06, RN-07, RN-08, RN-09, RN-14, RN-18, RN-20.
     */
    public Prestamo confirmarPrestamo(String solicitante, Usuario usuarioRegistra, LocalDate fechaEsperada,
                                      List<Integer> idsEquipo, String observaciones) {

        Autorizacion.exigirOperador(usuarioRegistra);
        validarDatosBasicos(solicitante, usuarioRegistra, fechaEsperada, idsEquipo);

        try (Connection con = ConexionDB.conectar()) {
            con.setAutoCommit(false);
            try {
                Prestamo prestamo = new Prestamo();
                prestamo.setSolicitante(solicitante.trim());
                prestamo.setUsuarioRegistra(usuarioRegistra);
                prestamo.setFechaPrestamo(LocalDateTime.now());
                prestamo.setFechaEsperadaDevolucion(fechaEsperada);
                prestamo.setEstado(EstadoPrestamo.ACTIVO);
                prestamo.setObservaciones(observaciones);

                // 1) Validar TODOS los equipos antes de escribir nada
                for (Integer idEquipo : idsEquipo) {
                    Equipo equipo = equipoRepository.buscarPorIdParaActualizar(con, idEquipo)
                            .orElseThrow(() -> new BusinessException(
                                    "El equipo con ID " + idEquipo + " no existe."));

                    if (equipo.getEstado() != EstadoEquipo.DISPONIBLE) {
                        throw new BusinessException("El equipo " + equipo.getCodigoInventario()
                                + " no se puede prestar porque está " + equipo.getEstado() + ".");
                    }
                    prestamo.agregarDetalle(new DetallePrestamo(null, null, equipo, EstadoDetalle.PENDIENTE));
                }

                // 2) Escribir encabezado, detalle y cambiar estados
                prestamo.setIdPrestamo(prestamoRepository.guardar(con, prestamo));

                for (DetallePrestamo detalle : prestamo.getDetalles()) {
                    detalle.setIdDetalle(prestamoRepository.guardarDetalle(con, detalle));
                    equipoRepository.actualizarEstado(con, detalle.getEquipo().getIdEquipo(), EstadoEquipo.PRESTADO);
                    detalle.getEquipo().setEstado(EstadoEquipo.PRESTADO);
                }

                // 3) Auditoría dentro de la misma transacción (RN-18)
                auditoriaRepository.registrar(con, usuarioRegistra.getIdUsuario(),
                        AccionAuditoria.PRESTAMO_CREADO.name(), "Prestamo", prestamo.getIdPrestamo(),
                        "Préstamo para " + prestamo.getSolicitante() + " con "
                                + prestamo.getDetalles().size() + " equipo(s).");

                con.commit();
                return prestamo;

            } catch (BusinessException | DataAccessException e) {
                rollbackSilencioso(con);
                throw e;
            } catch (RuntimeException e) {
                rollbackSilencioso(con);
                throw new DataAccessException("No se pudo confirmar el préstamo.", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo confirmar el préstamo.", e);
        }
    }

    private void validarDatosBasicos(String solicitante, Usuario usuario, LocalDate fechaEsperada, List<Integer> ids) {
        if (solicitante == null || solicitante.isBlank()) {
            throw new BusinessException("El solicitante es obligatorio.");
        }
        if (solicitante.trim().length() > 300) {
            throw new BusinessException("El nombre del solicitante es demasiado largo.");
        }
        if (usuario == null || usuario.getIdUsuario() == null) {
            throw new BusinessException("No se identificó al usuario que registra el préstamo.");
        }
        if (fechaEsperada == null || fechaEsperada.isBefore(LocalDate.now())) {
            throw new BusinessException("La fecha esperada de devolución no puede ser anterior a hoy.");
        }
        if (ids == null || ids.isEmpty()) {                                   // RN-05
            throw new BusinessException("El préstamo debe incluir al menos un equipo.");
        }
        Set<Integer> vistos = new HashSet<>();
        for (Integer id : ids) {
            if (id == null || id <= 0) {
                throw new BusinessException("Hay un equipo inválido en la lista.");
            }
            if (!vistos.add(id)) {                                            // RN-06
                throw new BusinessException("El mismo equipo no puede aparecer dos veces en el préstamo.");
            }
        }
    }

    private void rollbackSilencioso(Connection con) {
        try {
            con.rollback();
        } catch (SQLException ignorada) {
            // Si el rollback falla, al cerrar la conexión SQL Server descarta la transacción.
        }
    }
}
