package es.dam.hotelsol.basica.metrics;

import java.util.List;

/**
 * Registro estructurado de una prueba. Es el formato comun que entiende
 * el servidor de metricas y la pagina web.
 */
public final class RegistroPrueba {

    public static final String CABECERA_CSV =
            "registrado_en,id,participante,version,orden,inicio,fin,duracion_ms,duracion_seg,"
                    + "busquedas,errores,exito,completada,abandonada,resultado,primer_error_ms,"
                    + "recuperacion_ms,eventos_json";

    public String id;
    public String participante;
    public String version;
    public String orden;
    public String inicio;
    public String fin;
    public long duracionMs;
    public double duracionSeg;
    public int busquedas;
    public int errores;
    public boolean exito;
    public boolean completada;
    public boolean abandonada;
    public String resultado;
    public Long primerErrorMs;
    public Long recuperacionMs;
    public String registradoEn;
    public List<EventoPrueba> eventos;

    public String toJson() {
        StringBuilder sb = new StringBuilder(512);
        sb.append('{')
                .append("\"id\":").append(Json.texto(id)).append(',')
                .append("\"participante\":").append(Json.texto(participante)).append(',')
                .append("\"version\":").append(Json.texto(version)).append(',')
                .append("\"orden\":").append(Json.texto(orden)).append(',')
                .append("\"inicio\":").append(Json.texto(inicio)).append(',')
                .append("\"fin\":").append(Json.texto(fin)).append(',')
                .append("\"duracionMs\":").append(duracionMs).append(',')
                .append("\"duracionSeg\":").append(String.format(java.util.Locale.ROOT, "%.2f", duracionSeg)).append(',')
                .append("\"busquedas\":").append(busquedas).append(',')
                .append("\"errores\":").append(errores).append(',')
                .append("\"exito\":").append(exito).append(',')
                .append("\"completada\":").append(completada).append(',')
                .append("\"abandonada\":").append(abandonada).append(',')
                .append("\"resultado\":").append(Json.texto(resultado)).append(',')
                .append("\"primerErrorMs\":").append(Json.numero(primerErrorMs)).append(',')
                .append("\"recuperacionMs\":").append(Json.numero(recuperacionMs)).append(',')
                .append("\"registradoEn\":").append(Json.texto(registradoEn)).append(',')
                .append("\"eventos\":[");
        if (eventos != null) {
            for (int i = 0; i < eventos.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(eventos.get(i).toJson());
            }
        }
        return sb.append("]}").toString();
    }

    /** Fila CSV; todos los campos entre comillas para admitir comas y saltos de linea. */
    public String toCsvRow() {
        StringBuilder eventosJson = new StringBuilder("[");
        if (eventos != null) {
            for (int i = 0; i < eventos.size(); i++) {
                if (i > 0) {
                    eventosJson.append(',');
                }
                eventosJson.append(eventos.get(i).toJson());
            }
        }
        eventosJson.append(']');

        Object[] campos = {
                registradoEn, id, participante, version, orden, inicio, fin,
                duracionMs, String.format(java.util.Locale.ROOT, "%.2f", duracionSeg),
                busquedas, errores, exito, completada, abandonada, resultado,
                primerErrorMs == null ? "" : primerErrorMs,
                recuperacionMs == null ? "" : recuperacionMs,
                eventosJson.toString()
        };
        StringBuilder sb = new StringBuilder(512);
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(String.valueOf(campos[i]).replace("\"", "\"\"")).append('"');
        }
        return sb.toString();
    }
}
