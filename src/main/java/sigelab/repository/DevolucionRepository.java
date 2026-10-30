package sigelab.repository;

import sigelab.enums.CondicionDevolucion;
import sigelab.enums.EstadoDetalle;
import sigelab.enums.EstadoEquipo;
import sigelab.enums.EstadoPrestamo;
import sigelab.exception.DataAccessException;
import sigelab.model.DetallePrestamo;
import sigelab.model.Equipo;
import sigelab.model.Prestamo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DevolucionRepository {

    /** Datos mínimos de un detalle, leídos con bloqueo dentro de la transacción. */
    public record DetalleDevolucion(int idDetalle, int idPrestamo, int idEquipo, String codigoEquipo,
                                    EstadoDetalle estadoDetalle, EstadoPrestamo estadoPrestamo) {
    }

    /* ---------- Consultas para la pantalla (conexión propia) ---------- */

    public List<Prestamo> listarPrestamosActivos() {
        String sql = """
                SELECT id_prestamo, solicitante, fecha_prestamo, fecha_esperada_devolucion, estado
                FROM Prestamo
                WHERE estado = 'ACTIVO'
                ORDER BY id_prestamo
                """;
        List<Prestamo> lista = new ArrayList<>();
        try (Connection con = sigelab.config.ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Prestamo p = new Prestamo();
                p.setIdPrestamo(rs.getInt("id_prestamo"));
                p.setSolicitante(rs.getString("solicitante"));
                p.setFechaPrestamo(rs.getObject("fecha_prestamo", LocalDateTime.class));
                p.setFechaEsperadaDevolucion(rs.getObject("fecha_esperada_devolucion", LocalDate.class));
                p.setEstado(EstadoPrestamo.valueOf(rs.getString("estado")));
                lista.add(p);
            }
            return lista;
        } catch (SQLException e) {
            throw new DataAccessException("No se pudieron consultar los préstamos activos.", e);
        }
    }

    /** Equipos de un préstamo que todavía no han sido devueltos. */
    public List<DetallePrestamo> listarPendientes(int idPrestamo) {
        String sql = """
                SELECT d.id_detalle, d.id_prestamo, d.estado AS estado_detalle,
                       e.id_equipo, e.codigo_inventario, e.nombre, e.estado AS estado_equipo
                FROM DetallePrestamo d
                JOIN Equipo e ON e.id_equipo = d.id_equipo
                WHERE d.id_prestamo = ? AND d.estado = 'PENDIENTE'
                ORDER BY e.codigo_inventario
                """;
        List<DetallePrestamo> lista = new ArrayList<>();
        try (Connection con = sigelab.config.ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Equipo equipo = new Equipo();
                    equipo.setIdEquipo(rs.getInt("id_equipo"));
                    equipo.setCodigoInventario(rs.getString("codigo_inventario"));
                    equipo.setNombre(rs.getString("nombre"));
                    equipo.setEstado(EstadoEquipo.valueOf(rs.getString("estado_equipo")));

                    Prestamo prestamo = new Prestamo();
                    prestamo.setIdPrestamo(rs.getInt("id_prestamo"));

                    lista.add(new DetallePrestamo(rs.getInt("id_detalle"), prestamo, equipo,
                            EstadoDetalle.valueOf(rs.getString("estado_detalle"))));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DataAccessException("No se pudieron consultar los equipos pendientes.", e);
        }
    }

    /* ---------- Operaciones dentro de la transacción (reciben Connection) ---------- */

    /** Lee el detalle con UPDLOCK: dos devoluciones simultáneas del mismo equipo no pueden pasar a la vez. */
    public Optional<DetalleDevolucion> buscarDetalleParaDevolver(Connection con, int idDetalle) {
        String sql = """
                SELECT d.id_detalle, d.id_prestamo, d.id_equipo, d.estado AS estado_detalle,
                       p.estado AS estado_prestamo, e.codigo_inventario
                FROM DetallePrestamo d WITH (UPDLOCK, ROWLOCK)
                JOIN Prestamo p ON p.id_prestamo = d.id_prestamo
                JOIN Equipo e ON e.id_equipo = d.id_equipo
                WHERE d.id_detalle = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new DetalleDevolucion(
                        rs.getInt("id_detalle"),
                        rs.getInt("id_prestamo"),
                        rs.getInt("id_equipo"),
                        rs.getString("codigo_inventario"),
                        EstadoDetalle.valueOf(rs.getString("estado_detalle")),
                        EstadoPrestamo.valueOf(rs.getString("estado_prestamo"))));
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo consultar el detalle del préstamo.", e);
        }
    }

    public void guardarDevolucion(Connection con, int idDetalle, CondicionDevolucion condicion,
                                  String observacion, int idUsuario) {
        String sql = """
        INSERT INTO Devolucion (id_detalle, fecha_devolucion, condicion, observacion, id_usuario_registra)
        VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDetalle);
            ps.setObject(2, LocalDateTime.now());
            ps.setString(3, condicion.name());
            ps.setString(4, observacion);
            ps.setInt(5, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo registrar la devolución.", e);
        }
    }

    public void marcarDetalleDevuelto(Connection con, int idDetalle) {
        String sql = "UPDATE DetallePrestamo SET estado = 'DEVUELTO' WHERE id_detalle = ? AND estado = 'PENDIENTE'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDetalle);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("El detalle " + idDetalle + " no estaba pendiente.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo actualizar el detalle del préstamo.", e);
        }
    }

    public int contarPendientes(Connection con, int idPrestamo) {
        String sql = "SELECT COUNT(*) FROM DetallePrestamo WHERE id_prestamo = ? AND estado = 'PENDIENTE'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudieron contar los equipos pendientes.", e);
        }
    }

    public void finalizarPrestamo(Connection con, int idPrestamo) {
        String sql = "UPDATE Prestamo SET estado = 'FINALIZADO' WHERE id_prestamo = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo finalizar el préstamo.", e);
        }
    }
}
