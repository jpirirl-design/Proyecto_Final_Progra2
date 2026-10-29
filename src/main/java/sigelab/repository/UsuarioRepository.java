
package sigelab.repository;

import sigelab.config.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {

    // LISTAR USUARIOS
    public List<Object[]> listar() throws SQLException {

        List<Object[]> usuarios = new ArrayList<>();

        String sql = """
                SELECT
                    u.id_usuario,
                    u.nombre,
                    u.apellido,
                    u.nombre_usuario,
                    u.correo,
                    r.nombre AS rol,
                    u.estado
                FROM Usuario u
                INNER JOIN Rol r ON u.id_rol = r.id_rol
                ORDER BY u.id_usuario
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(new Object[]{
                        rs.getInt("id_usuario"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("nombre_usuario"),
                        rs.getString("correo"),
                        rs.getString("rol"),
                        rs.getString("estado")
                });
            }
        }

        return usuarios;
    }

    // CONSULTAR ROLES
    public List<Object[]> listarRoles() throws SQLException {

        List<Object[]> roles = new ArrayList<>();

        String sql = """
                SELECT id_rol, nombre
                FROM Rol
                ORDER BY id_rol
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                roles.add(new Object[]{
                        rs.getInt("id_rol"),
                        rs.getString("nombre")
                });
            }
        }

        return roles;
    }

    // GUARDAR USUARIO
    public void guardar(
            String nombre,
            String apellido,
            String usuario,
            String correo,
            String passwordHash,
            int idRol,
            String estado
    ) throws SQLException {

        String sql = """
                INSERT INTO Usuario
                (nombre, apellido, nombre_usuario,
                 correo, contrasena_hash, id_rol, estado)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, usuario);
            ps.setString(4, correo);
            ps.setString(5, passwordHash);
            ps.setInt(6, idRol);
            ps.setString(7, estado);

            ps.executeUpdate();
        }
    }

    // EDITAR USUARIO SIN CAMBIAR CONTRASEÑA
    public void editar(
            int id,
            String nombre,
            String apellido,
            String usuario,
            String correo,
            int idRol,
            String estado
    ) throws SQLException {

        String sql = """
                UPDATE Usuario
                SET nombre = ?,
                    apellido = ?,
                    nombre_usuario = ?,
                    correo = ?,
                    id_rol = ?,
                    estado = ?
                WHERE id_usuario = ?
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, usuario);
            ps.setString(4, correo);
            ps.setInt(5, idRol);
            ps.setString(6, estado);
            ps.setInt(7, id);

            ps.executeUpdate();
        }
    }

    // ACTUALIZAR CONTRASEÑA
    public void actualizarPassword(
            int id,
            String passwordHash
    ) throws SQLException {

        String sql = """
                UPDATE Usuario
                SET contrasena_hash = ?
                WHERE id_usuario = ?
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, passwordHash);
            ps.setInt(2, id);

            ps.executeUpdate();
        }
    }

    // DESACTIVAR USUARIO
    public void desactivar(int id) throws SQLException {

        String sql = """
                UPDATE Usuario
                SET estado = 'INACTIVO'
                WHERE id_usuario = ?
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
