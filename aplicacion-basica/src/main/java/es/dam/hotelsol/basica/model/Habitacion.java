package es.dam.hotelsol.basica.model;

/**
 * Datos de una habitacion del hotel. Inmutable.
 */
public final class Habitacion {

    private final String numero;
    private final String estado;
    private final String planta;
    private final String tipo;
    private final int capacidad;
    private final double precioNoche;
    private final String vista;
    private final boolean accesible;

    public Habitacion(String numero, String estado, String planta, String tipo,
                      int capacidad, double precioNoche, String vista, boolean accesible) {
        this.numero = numero;
        this.estado = estado;
        this.planta = planta;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.precioNoche = precioNoche;
        this.vista = vista;
        this.accesible = accesible;
    }

    public String getNumero() { return numero; }
    public String getEstado() { return estado; }
    public String getPlanta() { return planta; }
    public String getTipo() { return tipo; }
    public int getCapacidad() { return capacidad; }
    public double getPrecioNoche() { return precioNoche; }
    public String getVista() { return vista; }
    public boolean isAccesible() { return accesible; }

    public boolean estaDisponible() {
        return "disponible".equalsIgnoreCase(estado);
    }

    /** Texto legible del estado, sin depender solo del color. */
    public String getEstadoLegible() {
        return switch (estado.toLowerCase()) {
            case "disponible" -> "Disponible";
            case "ocupada" -> "Ocupada (no disponible)";
            case "mantenimiento" -> "En mantenimiento (no disponible)";
            default -> estado;
        };
    }

    public String getPrecioLegible() {
        return String.format(java.util.Locale.forLanguageTag("es-ES"), "%.2f EUR", precioNoche);
    }
}
