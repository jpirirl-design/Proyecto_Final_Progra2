package sigelab.repository;

import sigelab.config.ConexionDB;
import sigelab.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;

public class AuditoriaRepository {

    /** Se ejecuta dentro de la transacción del Service: si el préstamo falla, la auditoría también se revierte. */
    public void registrar(Connection con, Integer idUsuario, String accion,
                          String entidad, Integer idRegistro, String descripcion) {
        String sql = """
                INSERT INTO Auditoria (id_usuario, fecha_hora, accion, entidad, id_registro, descripcion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (idUsuario == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, idUsuario);
            ps.setObject(2, LocalDateTime.now());
            ps.setString(3, accion);
            ps.setString(4, entidad);
            if (idRegistro == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, idRegistro);
            ps.setString(6, descripcion);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo registrar la auditoría.", e);
        }
    }

    /** Para eventos que no forman parte de otra transacción (login, desactivar usuario). */
    public void registrar(Integer idUsuario, String accion, String entidad,
                          Integer idRegistro, String descripcion) {
        try (Connection con = ConexionDB.conectar()) {
            registrar(con, idUsuario, accion, entidad, idRegistro, descripcion);
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo registrar la auditoría.", e);
        }
    }
}
