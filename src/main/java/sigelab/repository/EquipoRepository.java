
package sigelab.repository;

import sigelab.config.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EquipoRepository {

    public List<Object[]> listarOpciones(String tabla)
            throws SQLException {

        if (!tabla.equals("TipoEquipo") &&
                !tabla.equals("Laboratorio")) {
            throw new SQLException("Tabla no permitida.");
        }

        String id = tabla.equals("TipoEquipo")
                ? "id_tipo_equipo" : "id_laboratorio";

        String sql = "SELECT " + id + ", nombre FROM " + tabla
                + (tabla.equals("Laboratorio")
                ? " WHERE estado = 'ACTIVO'" : "")
                + " ORDER BY nombre";

        List<Object[]> datos = new ArrayList<>();

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                datos.add(new Object[]{
                        rs.getInt(1), rs.getString(2)
                });
            }
        }
        return datos;
    }

    public List<Object[]> listar() throws SQLException {

        List<Object[]> datos = new ArrayList<>();

        String sql = """
                SELECT e.id_equipo, e.codigo_inventario,
                       e.nombre, e.id_tipo_equipo,
                       e.marca, e.modelo, e.numero_serie,
                       e.id_laboratorio, e.estado,
                       e.observaciones
                FROM Equipo e
                ORDER BY e.id_equipo
                """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                datos.add(new Object[]{
                        rs.getInt(1), rs.getString(2),
                        rs.getString(3), rs.getInt(4),
                        rs.getString(5), rs.getString(6),
                        rs.getString(7), rs.getInt(8),
                        rs.getString(9), rs.getString(10)
                });
            }
        }
        return datos;
    }

    public void guardar(String codigo, String nombre, int tipo,
                        int laboratorio, String marca,
                        String modelo, String serie,
                        String estado, String observaciones)
            throws SQLException {

        String sql = """
                INSERT INTO Equipo
                (codigo_inventario, nombre, id_tipo_equipo,
                 id_laboratorio, marca, modelo, numero_serie,
                 estado, observaciones)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            completarParametros(ps, codigo, nombre, tipo,
                    laboratorio, marca, modelo, serie,
                    estado, observaciones);
            ps.executeUpdate();
        }
    }

    /** Actualiza los datos descriptivos; el estado lo cambian solo los procesos del Service. */
    public void editar(int id, String codigo, String nombre,
                       int tipo, int laboratorio, String marca,
                       String modelo, String serie,
                       String observaciones)
            throws SQLException {

        String sql = """
                UPDATE Equipo
                SET codigo_inventario = ?, nombre = ?,
                    id_tipo_equipo = ?, id_laboratorio = ?,
                    marca = ?, modelo = ?, numero_serie = ?,
                    observaciones = ?
                WHERE id_equipo = ?
                """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setInt(3, tipo);
            ps.setInt(4, laboratorio);
            ps.setString(5, marca);
            ps.setString(6, modelo);
            ps.setString(7, serie);
            ps.setString(8, observaciones);
            ps.setInt(9, id);

            if (ps.executeUpdate() == 0) {
                throw new SQLException("El equipo no existe.");
            }
        }
    }

    private void completarParametros(
            PreparedStatement ps, String codigo, String nombre,
            int tipo, int laboratorio, String marca,
            String modelo, String serie, String estado,
            String observaciones) throws SQLException {

        ps.setString(1, codigo);
        ps.setString(2, nombre);
        ps.setInt(3, tipo);
        ps.setInt(4, laboratorio);
        ps.setString(5, marca);
        ps.setString(6, modelo);
        ps.setString(7, serie);
        ps.setString(8, estado);
        ps.setString(9, observaciones);
    }

    public void darDeBaja(int id) throws SQLException {

        String sql = """
                UPDATE Equipo
                SET estado = 'BAJA'
                WHERE id_equipo = ?
                AND estado <> 'PRESTADO'
                """;

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() == 0) {
                throw new SQLException(
                        "El equipo no existe o está prestado.");
            }
        }
    }
}
