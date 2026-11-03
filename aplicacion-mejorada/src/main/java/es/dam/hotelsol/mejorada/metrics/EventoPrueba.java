package es.dam.hotelsol.mejorada.metrics;

/** Evento registrado durante una prueba (busqueda, error, intento de finalizar...). */
public final class EventoPrueba {

    private final long ms;
    private final String tipo;
    private final String consulta;
    private final String detalle;

    public EventoPrueba(long ms, String tipo, String consulta, String detalle) {
        this.ms = ms;
        this.tipo = tipo;
        this.consulta = consulta;
        this.detalle = detalle;
    }

    public long getMs() { return ms; }
    public String getTipo() { return tipo; }
    public String getConsulta() { return consulta; }
    public String getDetalle() { return detalle; }

    public String toJson() {
        return "{\"ms\":" + ms
                + ",\"tipo\":" + Json.texto(tipo)
                + ",\"consulta\":" + Json.texto(consulta)
                + ",\"detalle\":" + Json.texto(detalle) + "}";
    }
}
