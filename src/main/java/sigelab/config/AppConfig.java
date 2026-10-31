package sigelab.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/**
 * Lee config/application.properties (en la carpeta del proyecto).
 * Una variable de entorno tiene prioridad: db.user -> SIGELAB_DB_USER, facial.token -> SIGELAB_FACIAL_TOKEN.
 */
public final class AppConfig {

    private static final Path ARCHIVO = Path.of("config", "application.properties");
    private static final Properties VALORES = cargar();

    private AppConfig() {
    }

    private static Properties cargar() {
        Properties p = new Properties();
        if (Files.exists(ARCHIVO)) {
            try (Reader lector = Files.newBufferedReader(ARCHIVO, StandardCharsets.UTF_8)) {
                p.load(lector);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo leer " + ARCHIVO + ".", e);
            }
        }
        return p;
    }

    public static String get(String clave, String porDefecto) {
        String env = System.getenv("SIGELAB_" + clave.toUpperCase(Locale.ROOT).replace('.', '_'));
        if (env != null && !env.isBlank()) {
            return env;
        }
        return VALORES.getProperty(clave, porDefecto);
    }

    public static String requerido(String clave) {
        String valor = get(clave, "");
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Configure " + clave
                    + " en config/application.properties (o la variable SIGELAB_"
                    + clave.toUpperCase(Locale.ROOT).replace('.', '_') + ").");
        }
        return valor;
    }
}
