
package sigelab.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class PruebaConexion {

    public static void main(String[] args) {

        try (Connection conexion = ConexionDB.conectar();
             Statement consulta = conexion.createStatement();
             ResultSet resultado = consulta.executeQuery(
                     "SELECT nombre FROM Rol ORDER BY id_rol"
             )) {

            System.out.println("ROLES REGISTRADOS EN SIGELAB:");

            while (resultado.next()) {
                System.out.println(resultado.getString("nombre"));
            }

        } catch (Exception e) {
            System.out.println("ERROR DE CONEXIÓN:");
            System.out.println(e.getMessage());
        }
    }
}
