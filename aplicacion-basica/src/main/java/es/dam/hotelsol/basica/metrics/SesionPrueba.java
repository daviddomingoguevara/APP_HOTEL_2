package es.dam.hotelsol.basica.metrics;

import es.dam.hotelsol.basica.model.Habitacion;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * Cronometro y contador de errores de UNA prueba.
 *
 * Usa System.nanoTime (reloj monotonico) para medir duraciones, por lo que un
 * cambio de hora del sistema no altera el resultado.
 */
public final class SesionPrueba {

    public static final String HABITACION_OBJETIVO = "103";

    private final LongSupplier relojNanos;
    private final long inicioNanos;
    private final String inicioIso;
    private final String participante;
    private final String version;
    private final String orden;
    private final List<EventoPrueba> eventos = new ArrayList<>();

    private int busquedas;
    private int errores;
    private boolean encontrada;
    private boolean completada;
    private boolean abandonada;
    private boolean errorPendiente;
    private long inicioRachaErrorMs;
    private Long primerErrorMs;
    private Long recuperacionMs;
    private String estadoObjetivo;
    private long finNanos;
    private String finIso;
    private RegistroPrueba registro;

    public SesionPrueba(String participante, String version, String orden) {
        this(participante, version, orden, System::nanoTime);
    }

    /** Constructor para pruebas automaticas: permite inyectar un reloj falso. */
    SesionPrueba(String participante, String version, String orden, LongSupplier relojNanos) {
        this.participante = participante;
        this.version = version;
        this.orden = orden;
        this.relojNanos = relojNanos;
        this.inicioNanos = relojNanos.getAsLong();
        this.inicioIso = Instant.now().toString();
    }

    public String getParticipante() { return participante; }
    public String getVersion() { return version; }
    public String getOrden() { return orden; }
    public int getBusquedas() { return busquedas; }
    public int getErrores() { return errores; }
    public boolean isEncontrada() { return encontrada; }
    public boolean isFinalizada() { return completada || abandonada; }
    public String getEstadoObjetivo() { return estadoObjetivo; }

    public String habitacionObjetivo() {
        return HABITACION_OBJETIVO;
    }

    /**
     * Registra una busqueda. Solo la habitacion 103 se considera avance correcto.
     * Devuelve true si la busqueda ha localizado la habitacion objetivo.
     */
    public synchronized boolean registrarBusqueda(String consulta, Habitacion resultado) {
        if (isFinalizada()) {
            return false;
        }
        long ms = transcurridoMs();
        busquedas++;
        boolean acierto = resultado != null && HABITACION_OBJETIVO.equals(resultado.getNumero());
        eventos.add(new EventoPrueba(ms, acierto ? "busqueda-correcta" : "error-busqueda",
                consulta, resultado == null ? "habitacion-inexistente" : resultado.getEstado()));
        if (acierto) {
            encontrada = true;
            estadoObjetivo = resultado.getEstado();
            if (errorPendiente && recuperacionMs == null) {
                recuperacionMs = Math.max(0, ms - inicioRachaErrorMs);
            }
            errorPendiente = false;
        } else {
            registrarError(ms);
        }
        return acierto;
    }

    /**
     * Intento de finalizar. Devuelve true si la prueba puede completarse.
     * Si aun no se ha localizado la habitacion 103 se contabiliza como error.
     */
    public synchronized boolean intentarFinalizar() {
        if (isFinalizada()) {
            return false;
        }
        if (encontrada) {
            return true;
        }
        long ms = transcurridoMs();
        eventos.add(new EventoPrueba(ms, "error-intento-finalizar", "", "sin-habitacion-encontrada"));
        registrarError(ms);
        return false;
    }

    /** Cierra la prueba como completada. Devuelve null si no se encontro la habitacion. */
    public synchronized RegistroPrueba completar() {
        if (isFinalizada() || !encontrada) {
            return null;
        }
        finNanos = relojNanos.getAsLong();
        finIso = Instant.now().toString();
        completada = true;
        eventos.add(new EventoPrueba(transcurridoMs(), "prueba-completada", HABITACION_OBJETIVO, estadoObjetivo));
        registro = construirRegistro("encontrada-" + estadoObjetivo.toLowerCase(Locale.ROOT), true, false);
        return registro;
    }

    /** Cierra la prueba como abandonada (por ejemplo, al cerrar la ventana). */
    public synchronized RegistroPrueba abandonar() {
        if (isFinalizada()) {
            return null;
        }
        finNanos = relojNanos.getAsLong();
        finIso = Instant.now().toString();
        abandonada = true;
        eventos.add(new EventoPrueba(transcurridoMs(), "prueba-abandonada", "", "ventana-cerrada"));
        registro = construirRegistro("abandonada", false, true);
        return registro;
    }

    public synchronized RegistroPrueba getRegistro() {
        return registro;
    }

    public synchronized long transcurridoMs() {
        long fin = (completada || abandonada) ? finNanos : relojNanos.getAsLong();
        return Math.max(0, (fin - inicioNanos) / 1_000_000L);
    }

    private void registrarError(long ms) {
        errores++;
        if (primerErrorMs == null) {
            primerErrorMs = ms;
        }
        if (!errorPendiente) {
            errorPendiente = true;
            inicioRachaErrorMs = ms;
        }
    }

    private RegistroPrueba construirRegistro(String resultado, boolean exito, boolean abandono) {
        RegistroPrueba r = new RegistroPrueba();
        r.id = UUID.randomUUID().toString();
        r.participante = participante;
        r.version = version;
        r.orden = orden;
        r.inicio = inicioIso;
        r.fin = finIso;
        r.duracionMs = transcurridoMs();
        r.duracionSeg = Math.round(r.duracionMs / 10.0) / 100.0;
        r.busquedas = busquedas;
        r.errores = errores;
        r.exito = exito;
        r.completada = completada;
        r.abandonada = abandono;
        r.resultado = resultado;
        r.primerErrorMs = primerErrorMs;
        r.recuperacionMs = recuperacionMs;
        r.registradoEn = Instant.now().toString();
        r.eventos = List.copyOf(eventos);
        return r;
    }
}
