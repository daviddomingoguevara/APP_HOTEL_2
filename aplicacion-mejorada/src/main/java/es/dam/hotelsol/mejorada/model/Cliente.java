package es.dam.hotelsol.mejorada.model;

/**
 * Cliente ficticio del hotel, obtenido de las reservas de ejemplo.
 */
public final class Cliente {

    private final String nombre;
    private final String email;
    private final int reservas;

    public Cliente(String nombre, String email, int reservas) {
        this.nombre = nombre;
        this.email = email;
        this.reservas = reservas;
    }

    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public int getReservas() { return reservas; }
}
