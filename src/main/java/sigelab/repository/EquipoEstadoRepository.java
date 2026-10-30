package sigelab.repository;

import sigelab.config.ConexionDB;
import sigelab.enums.EstadoEquipo;
import sigelab.exception.DataAccessException;
import sigelab.model.Equipo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Acceso a equipos orientado a préstamos, devoluciones, mantenimiento y QR.
 * Es independiente de EquipoRepository (Etapa 2) para no generar conflictos en Git.
 */
public class EquipoEstadoRepository {

    private static final String COLUMNAS =
            "id_equipo, codigo_inventario, nombre, marca, modelo, numero_serie, estado, observaciones";

    /** Consulta con su propia conexión. La usa la lectura de QR (RN-19: solo identifica). */
    public Optional<Equipo> buscarPorCodigo(String codigoInventario) {
        String sql = "SELECT " + COLUMNAS + " FROM Equipo WHERE codigo_inventario = ?";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigoInventario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo buscar el equipo.", e);
        }
    }

    /** Lectura dentro de una transacción; UPDLOCK impide que dos usuarios presten el mismo equipo a la vez. */
    public Optional<Equipo> buscarPorIdParaActualizar(Connection con, int idEquipo) {
        String sql = "SELECT " + COLUMNAS + " FROM Equipo WITH (UPDLOCK, ROWLOCK) WHERE id_equipo = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEquipo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo consultar el equipo.", e);
        }
    }

    /** Participa en la transacción del Service: recibe la Connection. */
    public void actualizarEstado(Connection con, int idEquipo, EstadoEquipo nuevoEstado) {
        String sql = "UPDATE Equipo SET estado = ? WHERE id_equipo = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, idEquipo);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("No se actualizó ninguna fila del equipo " + idEquipo);
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo actualizar el estado del equipo.", e);
        }
    }

    private Equipo mapear(ResultSet rs) throws SQLException {
        Equipo e = new Equipo();
        e.setIdEquipo(rs.getInt("id_equipo"));
        e.setCodigoInventario(rs.getString("codigo_inventario"));
        e.setNombre(rs.getString("nombre"));
        e.setMarca(rs.getString("marca"));
        e.setModelo(rs.getString("modelo"));
        e.setNumeroSerie(rs.getString("numero_serie"));
        e.setEstado(EstadoEquipo.valueOf(rs.getString("estado")));
        e.setObservaciones(rs.getString("observaciones"));
        return e;   // tipoEquipo y laboratorio quedan null: no se necesitan para prestar
    }
}