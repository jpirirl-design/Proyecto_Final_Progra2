package sigelab.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexiones JDBC a SQL Server con los datos de config/application.properties.
 * Siguen funcionando las variables SIGELAB_DB_USER y SIGELAB_DB_PASSWORD.
 */
public class ConexionDB {

    public static Connection conectar() throws SQLException {

        Properties p = new Properties();
        try {
            p.setProperty("serverName", AppConfig.get("db.host", "localhost"));
            p.setProperty("portNumber", AppConfig.get("db.port", "1433"));
            p.setProperty("databaseName", AppConfig.get("db.name", "SIGELAB"));
            p.setProperty("user", AppConfig.requerido("db.user"));
            p.setProperty("password", AppConfig.requerido("db.password"));
        } catch (IllegalStateException e) {
            throw new SQLException(e.getMessage());
        }
        p.setProperty("encrypt", AppConfig.get("db.encrypt", "true"));
        p.setProperty("trustServerCertificate", AppConfig.get("db.trustServerCertificate", "true"));
        p.setProperty("loginTimeout", "8");

        return DriverManager.getConnection("jdbc:sqlserver://", p);
    }
}
