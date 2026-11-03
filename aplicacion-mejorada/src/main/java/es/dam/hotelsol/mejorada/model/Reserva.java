package es.dam.hotelsol.mejorada.model;

/**
 * Reserva de ejemplo del fichero hotel.xml.
 */
public final class Reserva {

    private final String codigo;
    private final String estado;
    private final String cliente;
    private final String email;
    private final String habitacion;
    private final String entrada;
    private final String salida;
    private final int personas;
    private final String observaciones;

    public Reserva(String codigo, String estado, String cliente, String email,
                   String habitacion, String entrada, String salida, int personas,
                   String observaciones) {
        this.codigo = codigo;
        this.estado = estado;
        this.cliente = cliente;
        this.email = email;
        this.habitacion = habitacion;
        this.entrada = entrada;
        this.salida = salida;
        this.personas = personas;
        this.observaciones = observaciones;
    }

    public String getCodigo() { return codigo; }
    public String getEstado() { return estado; }
    public String getCliente() { return cliente; }
    public String getEmail() { return email; }
    public String getHabitacion() { return habitacion; }
    public String getEntrada() { return entrada; }
    public String getSalida() { return salida; }
    public int getPersonas() { return personas; }
    public String getObservaciones() { return observaciones; }
}
