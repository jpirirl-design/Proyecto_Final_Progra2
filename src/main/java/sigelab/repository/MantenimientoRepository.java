package sigelab.repository;

import sigelab.config.ConexionDB;
import sigelab.enums.EstadoMantenimiento;
import sigelab.enums.ResultadoMantenimiento;
import sigelab.enums.TipoMantenimiento;
import sigelab.exception.DataAccessException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MantenimientoRepository {

    /** Fila de mantenimiento leída con bloqueo, para cerrarla dentro de la transacción. */
    public record MantenimientoBloqueado(int idMantenimiento, int idEquipo, String codigoEquipo,
                                         EstadoMantenimiento estado) {
    }

    /** Fila para mostrar en pantalla (mantenimientos abiertos). */
    public record RegistroMantenimiento(int idMantenimiento, int idEquipo, String codigoEquipo,
                                        String nombreEquipo, TipoMantenimiento tipo, String descripcion,
                                        String responsable, LocalDate fechaIngreso,
                                        EstadoMantenimiento estado) {
    }

    /* ---------- Consulta para la pantalla (conexión propia) ---------- */

    public List<RegistroMantenimiento> listarEnProceso() {
        String sql = """
                SELECT m.id_mantenimiento, m.id_equipo, e.codigo_inventario, e.nombre,
                       m.tipo, m.descripcion, m.responsable, m.fecha_ingreso, m.estado
                FROM Mantenimiento m
                JOIN Equipo e ON e.id_equipo = m.id_equipo
                WHERE m.estado = 'EN_PROCESO'
                ORDER BY m.fecha_ingreso, m.id_mantenimiento
                """;
        List<RegistroMantenimiento> lista = new ArrayList<>();
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new RegistroMantenimiento(
                        rs.getInt("id_mantenimiento"),
                        rs.getInt("id_equipo"),
                        rs.getString("codigo_inventario"),
                        rs.getString("nombre"),
                        TipoMantenimiento.valueOf(rs.getString("tipo")),
                        rs.getString("descripcion"),
                        rs.getString("responsable"),
                        rs.getObject("fecha_ingreso", LocalDate.class),
                        EstadoMantenimiento.valueOf(rs.getString("estado"))));
            }
            return lista;
        } catch (SQLException e) {
            throw new DataAccessException("No se pudieron consultar los mantenimientos en proceso.", e);
        }
    }

    /* ---------- Operaciones dentro de la transacción (reciben Connection) ---------- */

    public int guardar(Connection con, int idEquipo, LocalDate fechaIngreso, TipoMantenimiento tipo,
                       String descripcion, String responsable, String observaciones) {
        String sql = """
                INSERT INTO Mantenimiento
                (id_equipo, fecha_ingreso, tipo, descripcion, responsable, estado, observaciones)
                VALUES (?, ?, ?, ?, ?, 'EN_PROCESO', ?)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idEquipo);
            ps.setObject(2, fechaIngreso);
            ps.setString(3, tipo.name());
            ps.setString(4, descripcion);
            ps.setString(5, responsable);
            ps.setString(6, observaciones);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se obtuvo el ID del mantenimiento.");
                }
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo registrar el mantenimiento.", e);
        }
    }

    /** UPDLOCK: dos usuarios no pueden cerrar el mismo mantenimiento a la vez. */
    public Optional<MantenimientoBloqueado> buscarParaFinalizar(Connection con, int idMantenimiento) {
        String sql = """
                SELECT m.id_mantenimiento, m.id_equipo, m.estado, e.codigo_inventario
                FROM Mantenimiento m WITH (UPDLOCK, ROWLOCK)
                JOIN Equipo e ON e.id_equipo = m.id_equipo
                WHERE m.id_mantenimiento = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMantenimiento);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new MantenimientoBloqueado(
                        rs.getInt("id_mantenimiento"),
                        rs.getInt("id_equipo"),
                        rs.getString("codigo_inventario"),
                        EstadoMantenimiento.valueOf(rs.getString("estado"))));
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo consultar el mantenimiento.", e);
        }
    }

    /**
     * Cierra el mantenimiento. estado, fecha_salida y resultado se cambian juntos
     * porque la restricción CK_Mant_cierre los exige consistentes.
     */
    public void finalizar(Connection con, int idMantenimiento, LocalDate fechaSalida,
                          ResultadoMantenimiento resultado, String observaciones) {
        String sql = """
                UPDATE Mantenimiento
                SET estado = 'FINALIZADO', fecha_salida = ?, resultado = ?,
                    observaciones = COALESCE(?, observaciones)
                WHERE id_mantenimiento = ? AND estado = 'EN_PROCESO'
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, fechaSalida);
            ps.setString(2, resultado.name());
            ps.setString(3, observaciones);
            ps.setInt(4, idMantenimiento);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("El mantenimiento " + idMantenimiento + " no estaba en proceso.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo finalizar el mantenimiento.", e);
        }
    }
}
