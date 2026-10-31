package sigelab.facial;

import sigelab.config.AppConfig;
import sigelab.exception.BusinessException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Consume la API local de reconocimiento facial (facial-service, FastAPI + InsightFace).
 * Configuración: facial.url (por defecto http://127.0.0.1:8765) y facial.token,
 * que debe ser igual a FACIAL_API_TOKEN de facial-service/.env.
 */
public class ClienteFacial {

    private final String urlBase;
    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public ClienteFacial() {
        this(AppConfig.get("facial.url", "http://127.0.0.1:8765"));
    }

    public ClienteFacial(String urlBase) {
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
    }

    /** ¿El usuario ya tiene perfil facial guardado en SQL Server? */
    public boolean estaRegistrado(int idUsuario) {
        String r = llamar("/face/status/" + idUsuario, "GET", null);
        if (leerEntero(r, "userId") != idUsuario) {
            throw new BusinessException("Respuesta facial inválida.");
        }
        return leerBooleano(r, "enrolled");
    }

    /** Abre la cámara, pide el desafío de vida y guarda el perfil cifrado. */
    public String registrar(int idUsuario, boolean reemplazar) {
        String r = llamar("/face/enroll", "POST",
                "{\"userId\":" + idUsuario + ",\"replace\":" + reemplazar + "}");
        if (!leerBooleano(r, "success") || leerEntero(r, "userId") != idUsuario
                || !leerBooleano(r, "livenessPassed")) {
            throw new BusinessException("No se completó el registro facial.");
        }
        String mensaje = leerTexto(r, "message");
        return mensaje == null ? "Rostro registrado correctamente." : mensaje;
    }

    /** Abre la cámara y compara con el perfil del usuario. */
    public ResultadoFacial verificar(int idUsuario) {
        String r = llamar("/face/verify", "POST", "{\"userId\":" + idUsuario + "}");
        if (!leerBooleano(r, "success") || leerEntero(r, "userId") != idUsuario) {
            throw new BusinessException("Respuesta facial inválida. Acceso denegado.");
        }
        return new ResultadoFacial(
                leerBooleano(r, "matched"),
                leerBooleano(r, "livenessPassed"),
                leerNumero(r, "score"),
                leerTexto(r, "message"));
    }

    private String llamar(String ruta, String metodo, String json) {
        URI uri = URI.create(urlBase + ruta);
        if (!"http".equals(uri.getScheme())
                || !("127.0.0.1".equals(uri.getHost()) || "localhost".equals(uri.getHost()))) {
            throw new BusinessException("El servicio facial debe ejecutarse localmente.");
        }
        String token;
        try {
            token = AppConfig.requerido("facial.token");
        } catch (IllegalStateException e) {
            throw new BusinessException(e.getMessage());
        }

        HttpRequest peticion = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(240))
                .header("X-API-Key", token)
                .header("Content-Type", "application/json")
                .method(metodo, json == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(json))
                .build();
        try {
            HttpResponse<String> res = http.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                String detalle = leerTexto(res.body(), "detail");
                throw new BusinessException(detalle != null ? detalle
                        : "El servicio facial rechazó la solicitud.");
            }
            return res.body();
        } catch (IOException e) {
            throw new BusinessException("No se pudo conectar con el servicio facial (" + urlBase
                    + "). Inícielo con facial-service/start-facial.ps1 y revise el token.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("La sesión facial fue interrumpida.");
        }
    }

    // ---- Lectura simple del JSON de respuesta (sin librerías externas) ----

    static boolean leerBooleano(String json, String campo) {
        Matcher m = Pattern.compile("\"" + campo + "\"\\s*:\\s*(true|false)").matcher(json);
        return m.find() && Boolean.parseBoolean(m.group(1));
    }

    static Double leerNumero(String json, String campo) {
        Matcher m = Pattern.compile("\"" + campo + "\"\\s*:\\s*(-?[0-9.eE+-]+)").matcher(json);
        return m.find() ? Double.valueOf(m.group(1)) : null;
    }

    static int leerEntero(String json, String campo) {
        Double n = leerNumero(json, campo);
        return n == null ? -1 : n.intValue();
    }

    static String leerTexto(String json, String campo) {
        Matcher m = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").matcher(json);
        return m.find() ? desescapar(m.group(1)) : null;
    }

    private static String desescapar(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                switch (n) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (i + 4 < s.length()) {
                            sb.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                            i += 4;
                        }
                    }
                    default -> sb.append(n);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
