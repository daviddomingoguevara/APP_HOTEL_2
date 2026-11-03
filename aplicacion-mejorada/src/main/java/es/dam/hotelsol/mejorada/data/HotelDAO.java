package es.dam.hotelsol.mejorada.data;

import es.dam.hotelsol.mejorada.model.Cliente;
import es.dam.hotelsol.mejorada.model.Habitacion;
import es.dam.hotelsol.mejorada.model.Reserva;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Acceso a los datos del hotel cargados desde /datos/hotel.xml.
 * La vista nunca lee el XML directamente: solo usa esta clase.
 */
public final class HotelDAO {

    private static final HotelDAO INSTANCIA = new HotelDAO();

    private final List<Habitacion> habitaciones;
    private final List<Reserva> reservas;
    private final Map<String, Habitacion> porNumero;
    private final String nombreHotel;
    private final String categoria;
    private final String ciudad;
    private final String direccion;
    private final String telefono;
    private final String fechaDatos;

    private HotelDAO() {
        try (InputStream entrada = HotelDAO.class.getResourceAsStream("/datos/hotel.xml")) {
            if (entrada == null) {
                throw new IllegalStateException("No se encontro /datos/hotel.xml en el classpath");
            }
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            fabrica.setFeature("http://xml.org/sax/features/external-general-entities", false);
            fabrica.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            fabrica.setExpandEntityReferences(false);
            Document documento = fabrica.newDocumentBuilder().parse(entrada);
            Element raiz = documento.getDocumentElement();
            raiz.normalize();

            nombreHotel = raiz.getAttribute("nombre");
            categoria = raiz.getAttribute("categoria");
            fechaDatos = raiz.getAttribute("fechaDatos");
            ciudad = textoHijo(raiz, "ciudad");
            direccion = textoHijo(raiz, "direccion");
            telefono = textoHijo(raiz, "telefono");

            List<Habitacion> listaHabitaciones = new ArrayList<>();
            Map<String, Habitacion> indice = new LinkedHashMap<>();
            NodeList nodosHabitacion = documento.getElementsByTagName("habitacion");
            for (int i = 0; i < nodosHabitacion.getLength(); i++) {
                Element nodo = (Element) nodosHabitacion.item(i);
                Habitacion habitacion = new Habitacion(
                        nodo.getAttribute("numero"),
                        nodo.getAttribute("estado"),
                        nodo.getAttribute("planta"),
                        textoHijo(nodo, "tipo"),
                        entero(textoHijo(nodo, "capacidad")),
                        decimal(textoHijo(nodo, "precioNoche")),
                        textoHijo(nodo, "vista"),
                        "si".equalsIgnoreCase(sinAcentos(textoHijo(nodo, "accesible")))
                                || "sí".equalsIgnoreCase(textoHijo(nodo, "accesible"))
                );
                listaHabitaciones.add(habitacion);
                indice.put(habitacion.getNumero(), habitacion);
            }
            habitaciones = List.copyOf(listaHabitaciones);
            porNumero = Collections.unmodifiableMap(indice);

            List<Reserva> listaReservas = new ArrayList<>();
            NodeList nodosReserva = documento.getElementsByTagName("reserva");
            for (int i = 0; i < nodosReserva.getLength(); i++) {
                Element nodo = (Element) nodosReserva.item(i);
                Element cliente = (Element) nodo.getElementsByTagName("cliente").item(0);
                String nombre = textoHijo(cliente, "nombre") + " " + textoHijo(cliente, "apellidos");
                listaReservas.add(new Reserva(
                        nodo.getAttribute("codigo"),
                        nodo.getAttribute("estado"),
                        nombre.trim(),
                        textoHijo(cliente, "email"),
                        textoHijo(nodo, "habitacionReservada"),
                        textoHijo(nodo, "entrada"),
                        textoHijo(nodo, "salida"),
                        entero(textoHijo(nodo, "personas")),
                        textoHijo(nodo, "observaciones")
                ));
            }
            reservas = List.copyOf(listaReservas);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudieron cargar los datos del hotel", e);
        }
    }

    public static HotelDAO getInstance() {
        return INSTANCIA;
    }

    public List<Habitacion> getHabitaciones() { return habitaciones; }
    public List<Reserva> getReservas() { return reservas; }
    public String getNombreHotel() { return nombreHotel; }
    public String getCategoria() { return categoria; }
    public String getCiudad() { return ciudad; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getFechaDatos() { return fechaDatos; }

    /** Devuelve la habitacion o null si el numero no existe. */
    public Habitacion buscar(String numero) {
        if (numero == null) {
            return null;
        }
        return porNumero.get(numero.trim());
    }

    /** Numero de habitaciones por estado, util para la pantalla de inicio. */
    public long contarPorEstado(String estado) {
        return habitaciones.stream()
                .filter(h -> h.getEstado().equalsIgnoreCase(estado))
                .count();
    }

    /** Clientes unicos a partir de las reservas de ejemplo. */
    public List<Cliente> getClientes() {
        Map<String, int[]> contador = new LinkedHashMap<>();
        Map<String, String> nombres = new LinkedHashMap<>();
        for (Reserva reserva : reservas) {
            contador.computeIfAbsent(reserva.getEmail(), k -> new int[]{0})[0]++;
            nombres.putIfAbsent(reserva.getEmail(), reserva.getCliente());
        }
        List<Cliente> clientes = new ArrayList<>();
        contador.forEach((email, veces) -> clientes.add(new Cliente(nombres.get(email), email, veces[0])));
        return List.copyOf(clientes);
    }

    private static String textoHijo(Element padre, String etiqueta) {
        NodeList hijos = padre.getChildNodes();
        for (int i = 0; i < hijos.getLength(); i++) {
            Node nodo = hijos.item(i);
            if (nodo.getNodeType() == Node.ELEMENT_NODE && etiqueta.equals(nodo.getNodeName())) {
                return nodo.getTextContent().trim();
            }
        }
        return "";
    }

    private static int entero(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double decimal(String valor) {
        try {
            return Double.parseDouble(valor.replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String sinAcentos(String texto) {
        return texto.toLowerCase().replace("í", "i").replace("á", "a")
                .replace("é", "e").replace("ó", "o").replace("ú", "u");
    }
}
