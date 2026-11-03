package es.dam.hotelsol.basica.metrics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Envia el registro al servidor y, si no esta disponible, lo guarda en un CSV
 * local para poder importarlo despues desde la web.
 */
public final class EnvioMetricas {

    public enum Estado { ENVIADO, GUARDADO_LOCAL, ERROR }

    public record Resultado(Estado estado, String mensaje) {
    }

    private EnvioMetricas() {
    }

    public static Path rutaCsvLocal() {
        return Path.of(System.getProperty("user.home"), ".trabajo2-usabilidad", "resultados-locales.csv");
    }

    public static synchronized Resultado enviar(RegistroPrueba registro) {
        if (ApiCliente.enviar(registro)) {
            return new Resultado(Estado.ENVIADO,
                    "Resultado enviado al servidor de metricas.");
        }
        try {
            Path ruta = rutaCsvLocal();
            Files.createDirectories(ruta.getParent());
            boolean nuevo = !Files.exists(ruta) || Files.size(ruta) == 0;
            StringBuilder sb = new StringBuilder();
            if (nuevo) {
                sb.append('\uFEFF').append(RegistroPrueba.CABECERA_CSV).append("\r\n");
            }
            sb.append(registro.toCsvRow()).append("\r\n");
            Files.writeString(ruta, sb.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return new Resultado(Estado.GUARDADO_LOCAL,
                    "Servidor no disponible. Resultado guardado en: " + ruta);
        } catch (IOException e) {
            return new Resultado(Estado.ERROR,
                    "No se pudo enviar ni guardar el resultado: " + e.getMessage());
        }
    }
}
