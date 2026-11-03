package es.dam.hotelsol.basica.data;

import es.dam.hotelsol.basica.model.Habitacion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HotelDAOTest {

    @Test
    void cargaLasTreintaHabitacionesYLasTreintaReservas() {
        HotelDAO dao = HotelDAO.getInstance();
        assertEquals(30, dao.getHabitaciones().size());
        assertEquals(30, dao.getReservas().size());
        assertEquals("Hotel Sol", dao.getNombreHotel());
        assertEquals("Madrid", dao.getCiudad());
    }

    @Test
    void laHabitacion103ExisteYEstaOcupadaSegunElXml() {
        Habitacion h = HotelDAO.getInstance().buscar("103");
        assertNotNull(h);
        assertEquals("103", h.getNumero());
        assertEquals("ocupada", h.getEstado());
        assertFalse(h.estaDisponible());
        assertEquals("Doble", h.getTipo());
        assertTrue(h.isAccesible());
        assertEquals("1", h.getPlanta());
    }

    @Test
    void unaHabitacionInexistenteDevuelveNull() {
        assertNull(HotelDAO.getInstance().buscar("999"));
        assertNull(HotelDAO.getInstance().buscar(""));
        assertNull(HotelDAO.getInstance().buscar(null));
    }

    @Test
    void losClientesSeObtienenDeLasReservas() {
        assertEquals(30, HotelDAO.getInstance().getClientes().size());
    }
}
