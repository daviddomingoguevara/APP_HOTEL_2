package es.dam.hotelsol.basica.metrics;

import es.dam.hotelsol.basica.model.Habitacion;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class SesionPruebaTest {

    private final Habitacion h101 = new Habitacion("101", "ocupada", "1", "Individual", 1, 75, "Patio", false);
    private final Habitacion h103 = new Habitacion("103", "ocupada", "1", "Doble", 2, 115, "Patio", true);

    @Test
    void registraErroresRecuperacionYDuracionConRelojMonotonico() {
        AtomicLong reloj = new AtomicLong(0);
        SesionPrueba sesion = new SesionPrueba("P01", "A", "A-B", reloj::get);

        reloj.set(1_000_000_000L); // 1 s
        assertFalse(sesion.registrarBusqueda("101", h101));
        assertEquals(1, sesion.getErrores());
        assertEquals(1, sesion.getBusquedas());

        reloj.set(3_000_000_000L); // 3 s
        assertTrue(sesion.registrarBusqueda("103", h103));
        assertEquals(1, sesion.getErrores());
        assertTrue(sesion.isEncontrada());

        reloj.set(4_500_000_000L); // 4,5 s
        RegistroPrueba registro = sesion.completar();
        assertNotNull(registro);
        assertTrue(registro.exito);
        assertTrue(registro.completada);
        assertFalse(registro.abandonada);
        assertEquals(4500, registro.duracionMs);
        assertEquals(4.5, registro.duracionSeg);
        assertEquals(1000L, registro.primerErrorMs);
        assertEquals(2000L, registro.recuperacionMs);
        assertEquals("encontrada-ocupada", registro.resultado);
        assertTrue(registro.toJson().contains("\"participante\":\"P01\""));
    }

    @Test
    void finalizarSinEncontrarCuentaComoError() {
        AtomicLong reloj = new AtomicLong(0);
        SesionPrueba sesion = new SesionPrueba("P02", "A", "B-A", reloj::get);
        reloj.set(2_000_000_000L);
        assertFalse(sesion.intentarFinalizar());
        assertEquals(1, sesion.getErrores());
        assertNull(sesion.completar());
    }

    @Test
    void unaPruebaAbandonadaNoEsUnExito() {
        AtomicLong reloj = new AtomicLong(0);
        SesionPrueba sesion = new SesionPrueba("P03", "B", "B-A", reloj::get);
        reloj.set(1_000_000_000L);
        sesion.registrarBusqueda("999", null);
        reloj.set(5_000_000_000L);
        RegistroPrueba registro = sesion.abandonar();
        assertNotNull(registro);
        assertFalse(registro.exito);
        assertFalse(registro.completada);
        assertTrue(registro.abandonada);
        assertEquals("abandonada", registro.resultado);
        assertEquals(5000, registro.duracionMs);
    }

    @Test
    void noSePuedeRegistrarNadaTrasFinalizar() {
        AtomicLong reloj = new AtomicLong(0);
        SesionPrueba sesion = new SesionPrueba("P04", "A", "A-B", reloj::get);
        sesion.abandonar();
        assertFalse(sesion.registrarBusqueda("103", h103));
        assertNull(sesion.completar());
    }
}
