package es.dam.hotelsol.mejorada.metrics;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cliente REST del servidor local de metricas.
 * La URL se puede cambiar con la propiedad -Dtrabajo2.api=... o la variable TRABAJO2_API.
 */
public final class ApiCliente {

    private static final Pattern ORDEN = Pattern.compile("\"orden\"\\s*:\\s*\"([^\"]*)\"");
    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(1200))
            .build();

    private ApiCliente() {
    }

    public static String baseUrl() {
        String valor = System.getProperty("trabajo2.api");
        if (valor == null || valor.isBlank()) {
            valor = System.getenv("TRABAJO2_API");
        }
        if (valor == null || valor.isBlank()) {
            valor = "http://localhost:8080";
        }
        return valor.replaceAll("/+$", "");
    }

    /**
     * Obtiene el orden de pruebas asignado al participante. Si aun no existe,
     * lo registra. Devuelve "no-disponible" si el servidor no responde.
     */
    public static String obtenerOCrearOrden(String participante) {
        try {
            String id = URLEncoder.encode(participante, StandardCharsets.UTF_8);
            HttpRequest peticion = HttpRequest.newBuilder(URI.create(baseUrl() + "/api/participantes/" + id))
                    .timeout(Duration.ofSeconds(2))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> respuesta = CLIENTE.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (respuesta.statusCode() == 200) {
                return extraerOrden(respuesta.body());
            }
            if (respuesta.statusCode() == 404) {
                return crearParticipante(participante);
            }
        } catch (Exception e) {
            // Sin servidor: la prueba puede hacerse igualmente en modo local.
        }
        return "no-disponible";
    }

    private static String crearParticipante(String participante) {
        try {
            String cuerpo = "{\"id\":" + textoJson(participante) + "}";
            HttpRequest peticion = HttpRequest.newBuilder(URI.create(baseUrl() + "/api/participantes"))
                    .timeout(Duration.ofSeconds(2))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpo, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> respuesta = CLIENTE.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (respuesta.statusCode() >= 200 && respuesta.statusCode() < 300) {
                return extraerOrden(respuesta.body());
            }
        } catch (Exception e) {
            // Se devuelve no-disponible.
        }
        return "no-disponible";
    }

    /** Envia una prueba al servidor. Devuelve true si se almaceno correctamente. */
    public static boolean enviar(RegistroPrueba registro) {
        try {
            HttpRequest peticion = HttpRequest.newBuilder(URI.create(baseUrl() + "/api/pruebas"))
                    .timeout(Duration.ofSeconds(4))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(registro.toJson(), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> respuesta = CLIENTE.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return respuesta.statusCode() >= 200 && respuesta.statusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    private static String extraerOrden(String json) {
        Matcher m = ORDEN.matcher(json);
        return m.find() ? m.group(1) : "no-disponible";
    }

    private static String textoJson(String valor) {
        return "\"" + valor.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
