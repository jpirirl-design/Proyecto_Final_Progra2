package sigelab.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;" +
                    "databaseName=SIGELAB;" +
                    "encrypt=true;" +
                    "trustServerCertificate=true;";

    public static Connection conectar() throws SQLException {

        String usuario = System.getenv("SIGELAB_DB_USER");
        String password = System.getenv("SIGELAB_DB_PASSWORD");

        if (usuario == null || password == null) {
            throw new SQLException(
                    "Faltan las variables SIGELAB_DB_USER y SIGELAB_DB_PASSWORD."
            );
        }

        return DriverManager.getConnection(URL, usuario, password);
    }
}
