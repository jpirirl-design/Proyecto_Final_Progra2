package sigelab.repository;

import sigelab.exception.DataAccessException;
import sigelab.model.DetallePrestamo;
import sigelab.model.Prestamo;

import java.sql.*;

public class PrestamoRepository {

    /** Inserta el encabezado y devuelve el ID generado. Usa la Connection de la transacción. */
    public int guardar(Connection con, Prestamo p) {
        String sql = """
                INSERT INTO Prestamo
                (solicitante, fecha_prestamo, fecha_esperada_devolucion,
                 id_usuario_registra, estado, observaciones)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getSolicitante());
            ps.setObject(2, p.getFechaPrestamo());
            ps.setObject(3, p.getFechaEsperadaDevolucion());
            ps.setInt(4, p.getUsuarioRegistra().getIdUsuario());
            ps.setString(5, p.getEstado().name());
            ps.setString(6, p.getObservaciones());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se obtuvo el ID del préstamo.");
                }
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo registrar el préstamo.", e);
        }
    }

    /** Inserta una fila de detalle y devuelve su ID. */
    public int guardarDetalle(Connection con, DetallePrestamo d) {
        String sql = "INSERT INTO DetallePrestamo (id_prestamo, id_equipo, estado) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getPrestamo().getIdPrestamo());
            ps.setInt(2, d.getEquipo().getIdEquipo());
            ps.setString(3, d.getEstado().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se obtuvo el ID del detalle.");
                }
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo registrar el detalle del préstamo.", e);
        }
    }
}