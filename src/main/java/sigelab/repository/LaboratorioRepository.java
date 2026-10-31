
package sigelab.repository;

import sigelab.config.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LaboratorioRepository {

    public void guardar(String codigo, String nombre,
                        String ubicacion, int capacidad)
            throws SQLException {

        String sql = """
                INSERT INTO Laboratorio
                (codigo, nombre, ubicacion, capacidad)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setString(3, ubicacion);
            ps.setInt(4, capacidad);
            ps.executeUpdate();
        }
    }

    public List<Object[]> listar() throws SQLException {

        List<Object[]> laboratorios = new ArrayList<>();

        String sql = """
                SELECT id_laboratorio, codigo, nombre,
                       ubicacion, capacidad, estado
                FROM Laboratorio
                ORDER BY id_laboratorio
                """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                laboratorios.add(new Object[]{
                        rs.getInt("id_laboratorio"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("ubicacion"),
                        rs.getInt("capacidad"),
                        rs.getString("estado")
                });
            }
        }

        return laboratorios;
    }

    public void editar(int id, String codigo, String nombre,
                       String ubicacion, int capacidad, String estado)
            throws SQLException {

        String sql = """
                UPDATE Laboratorio
                SET codigo = ?, nombre = ?, ubicacion = ?,
                    capacidad = ?, estado = ?
                WHERE id_laboratorio = ?
                """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setString(3, ubicacion);
            ps.setInt(4, capacidad);
            ps.setString(5, estado);
            ps.setInt(6, id);

            if (ps.executeUpdate() == 0) {
                throw new SQLException("El laboratorio ya no existe.");
            }
        }
    }

    public void desactivar(int id) throws SQLException {

        String sql = """
                UPDATE Laboratorio
                SET estado = 'INACTIVO'
                WHERE id_laboratorio = ?
                """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() == 0) {
                throw new SQLException("El laboratorio ya no existe.");
            }
        }
    }
}
