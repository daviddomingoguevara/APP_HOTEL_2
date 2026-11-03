package es.dam.hotelsol.basica.controller;

import es.dam.hotelsol.basica.data.HotelDAO;
import es.dam.hotelsol.basica.metrics.EnvioMetricas;
import es.dam.hotelsol.basica.metrics.RegistroPrueba;
import es.dam.hotelsol.basica.metrics.SesionPrueba;
import es.dam.hotelsol.basica.model.Cliente;
import es.dam.hotelsol.basica.model.Habitacion;
import es.dam.hotelsol.basica.model.Reserva;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;

/**
 * Controlador de la version A. Mantiene la interfaz separada de los datos
 * (HotelDAO) y de la logica de metricas (SesionPrueba).
 */
public class MainController {

    @FXML private VBox seccionInicio;
    @FXML private VBox seccionHabitaciones;
    @FXML private VBox seccionReservas;
    @FXML private VBox seccionUsuarios;
    @FXML private VBox seccionConfiguracion;

    @FXML private Button navInicio;
    @FXML private Button navHabitaciones;
    @FXML private Button navReservas;
    @FXML private Button navUsuarios;
    @FXML private Button navConfiguracion;

    @FXML private TextField campoNumero;
    @FXML private Label lblMensajeResultado;
    @FXML private Label lblDetalleHabitacion;
    @FXML private Label lblParticipante;
    @FXML private Label lblRegistro;
    @FXML private Button botonFinalizar;

    @FXML private TableView<Reserva> tablaReservas;
    @FXML private TableColumn<Reserva, String> colReservaCodigo;
    @FXML private TableColumn<Reserva, String> colReservaCliente;
    @FXML private TableColumn<Reserva, String> colReservaHabitacion;
    @FXML private TableColumn<Reserva, String> colReservaEntrada;
    @FXML private TableColumn<Reserva, String> colReservaSalida;
    @FXML private TableColumn<Reserva, String> colReservaEstado;

    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, String> colClienteNombre;
    @FXML private TableColumn<Cliente, String> colClienteEmail;
    @FXML private TableColumn<Cliente, Number> colClienteReservas;

    @FXML private Label lblHotelNombre;
    @FXML private Label lblHotelCategoria;
    @FXML private Label lblHotelCiudad;
    @FXML private Label lblHotelDireccion;
    @FXML private Label lblHotelTelefono;
    @FXML private Label lblHotelFecha;

    private final HotelDAO dao = HotelDAO.getInstance();
    private SesionPrueba sesion;

    @FXML
    public void initialize() {
        configurarTablas();
        mostrarSeccion(seccionInicio, navInicio);
        lblMensajeResultado.setText("");
        lblDetalleHabitacion.setText("");
        lblRegistro.setText("");
        botonFinalizar.setDisable(true);

        lblHotelNombre.setText(dao.getNombreHotel());
        lblHotelCategoria.setText(dao.getCategoria());
        lblHotelCiudad.setText(dao.getCiudad());
        lblHotelDireccion.setText(dao.getDireccion());
        lblHotelTelefono.setText(dao.getTelefono());
        lblHotelFecha.setText(dao.getFechaDatos());
    }

    public void iniciarSesion(SesionPrueba nuevaSesion) {
        this.sesion = nuevaSesion;
        lblParticipante.setText("Participante: " + nuevaSesion.getParticipante());
        botonFinalizar.setDisable(false);
    }

    private void configurarTablas() {
        colReservaCodigo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCodigo()));
        colReservaCliente.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCliente()));
        colReservaHabitacion.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getHabitacion()));
        colReservaEntrada.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getEntrada()));
        colReservaSalida.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getSalida()));
        colReservaEstado.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getEstado()));
        tablaReservas.setItems(FXCollections.observableArrayList(dao.getReservas()));

        colClienteNombre.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNombre()));
        colClienteEmail.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getEmail()));
        colClienteReservas.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getReservas()));
        tablaClientes.setItems(FXCollections.observableArrayList(dao.getClientes()));
    }

    @FXML
    private void mostrarInicio() { mostrarSeccion(seccionInicio, navInicio); }

    @FXML
    private void mostrarHabitaciones() { mostrarSeccion(seccionHabitaciones, navHabitaciones); }

    @FXML
    private void mostrarReservas() { mostrarSeccion(seccionReservas, navReservas); }

    @FXML
    private void mostrarUsuarios() { mostrarSeccion(seccionUsuarios, navUsuarios); }

    @FXML
    private void mostrarConfiguracion() { mostrarSeccion(seccionConfiguracion, navConfiguracion); }

    private void mostrarSeccion(VBox seccion, Button botonActivo) {
        List<VBox> secciones = List.of(seccionInicio, seccionHabitaciones, seccionReservas,
                seccionUsuarios, seccionConfiguracion);
        for (VBox s : secciones) {
            boolean activa = s == seccion;
            s.setVisible(activa);
            s.setManaged(activa);
        }
        for (Button b : List.of(navInicio, navHabitaciones, navReservas, navUsuarios, navConfiguracion)) {
            b.getStyleClass().remove("activo");
        }
        if (botonActivo != null) {
            botonActivo.getStyleClass().add("activo");
        }
    }

    @FXML
    private void buscar() {
        if (sesion == null || sesion.isFinalizada()) {
            return;
        }
        String consulta = campoNumero.getText() == null ? "" : campoNumero.getText().trim();
        Habitacion habitacion = dao.buscar(consulta);
        boolean acierto = sesion.registrarBusqueda(consulta, habitacion);

        if (habitacion == null) {
            lblMensajeResultado.setText("La habitacion " + (consulta.isEmpty() ? "(vacío)" : consulta) + " no existe.");
            lblDetalleHabitacion.setText("");
            return;
        }
        lblDetalleHabitacion.setText(formatearHabitacion(habitacion));
        if (acierto) {
            lblMensajeResultado.setText("Habitacion 103 encontrada.");
        } else {
            lblMensajeResultado.setText("Resultado de la busqueda:");
        }
    }

    private String formatearHabitacion(Habitacion h) {
        return "Numero: " + h.getNumero()
                + "\nEstado: " + h.getEstado()
                + "\nPlanta: " + h.getPlanta()
                + "\nTipo: " + h.getTipo()
                + "\nCapacidad: " + h.getCapacidad() + " personas"
                + "\nPrecio por noche: " + h.getPrecioLegible()
                + "\nVista: " + h.getVista()
                + "\nAccesible: " + (h.isAccesible() ? "si" : "no");
    }

    @FXML
    private void finalizar() {
        if (sesion == null || sesion.isFinalizada()) {
            return;
        }
        if (!sesion.intentarFinalizar()) {
            lblMensajeResultado.setText("Todavia no has encontrado la habitacion 103.");
            return;
        }
        RegistroPrueba registro = sesion.completar();
        if (registro == null) {
            return;
        }
        botonFinalizar.setDisable(true);
        campoNumero.setDisable(true);
        lblMensajeResultado.setText("Prueba completada.");
        EnvioMetricas.Resultado envio = EnvioMetricas.enviar(registro);
        lblRegistro.setText(envio.mensaje());

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Prueba completada");
        alerta.setHeaderText("La prueba se ha registrado.");
        alerta.setContentText(String.format(Locale.forLanguageTag("es-ES"),
                "Tiempo: %.2f s%nBusquedas: %d%nErrores: %d%n%nPuedes cerrar esta ventana y continuar con la otra version.",
                registro.duracionSeg, registro.busquedas, registro.errores));
        alerta.showAndWait();
    }

    /** Se llama al cerrar la ventana: si la prueba estaba en curso, se marca abandonada. */
    public void abandonarSiProcede() {
        if (sesion == null || sesion.isFinalizada()) {
            return;
        }
        RegistroPrueba registro = sesion.abandonar();
        if (registro != null) {
            EnvioMetricas.enviar(registro);
        }
    }
}
