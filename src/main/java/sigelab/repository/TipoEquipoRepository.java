
package sigelab.repository;

import sigelab.config.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TipoEquipoRepository {

    public void guardar(String nombre, String descripcion)
            throws SQLException {

        String sql = """
                INSERT INTO TipoEquipo (nombre, descripcion)
                VALUES (?, ?)
                """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.executeUpdate();
        }
    }

    public List<Object[]> listar() throws SQLException {

        List<Object[]> tipos = new ArrayList<>();

        String sql = """
                SELECT id_tipo_equipo, nombre, descripcion
                FROM TipoEquipo
                ORDER BY id_tipo_equipo
                """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tipos.add(new Object[]{
                        rs.getInt("id_tipo_equipo"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                });
            }
        }

        return tipos;
    }

    public void editar(int id, String nombre, String descripcion)
            throws SQLException {

        String sql = """
                UPDATE TipoEquipo
                SET nombre = ?, descripcion = ?
                WHERE id_tipo_equipo = ?
                """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setInt(3, id);

            if (ps.executeUpdate() == 0) {
                throw new SQLException("El tipo de equipo no existe.");
            }
        }
    }

    public void eliminar(int id) throws SQLException {

        String sql = """
                DELETE FROM TipoEquipo
                WHERE id_tipo_equipo = ?
                AND NOT EXISTS (
                    SELECT 1 FROM Equipo
                    WHERE Equipo.id_tipo_equipo = TipoEquipo.id_tipo_equipo
                )
                """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() == 0) {
                throw new SQLException(
                        "No se pudo eliminar. El tipo no existe " +
                                "o tiene equipos asociados.");
            }
        }
    }
}
