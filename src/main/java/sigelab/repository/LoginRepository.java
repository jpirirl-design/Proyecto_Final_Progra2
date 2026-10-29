
package sigelab.repository;

import sigelab.config.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginRepository {

    public Object[] buscarPorUsuario(String usuario)
            throws SQLException {

        String sql = """
                SELECT
                    u.id_usuario,
                    u.nombre,
                    u.apellido,
                    u.nombre_usuario,
                    u.contrasena_hash,
                    u.estado,
                    r.nombre AS rol
                FROM Usuario u
                INNER JOIN Rol r ON u.id_rol = r.id_rol
                WHERE u.nombre_usuario = ?
                """;

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setString(1, usuario);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new Object[]{
                            rs.getInt("id_usuario"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("nombre_usuario"),
                            rs.getString("contrasena_hash"),
                            rs.getString("estado"),
                            rs.getString("rol")
                    };
                }
            }
        }

        return null;
    }
}
