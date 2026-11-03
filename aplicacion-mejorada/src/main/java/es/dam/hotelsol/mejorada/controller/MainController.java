package es.dam.hotelsol.mejorada.controller;

import es.dam.hotelsol.mejorada.App;
import es.dam.hotelsol.mejorada.data.HotelDAO;
import es.dam.hotelsol.mejorada.metrics.EnvioMetricas;
import es.dam.hotelsol.mejorada.metrics.RegistroPrueba;
import es.dam.hotelsol.mejorada.metrics.SesionPrueba;
import es.dam.hotelsol.mejorada.model.Cliente;
import es.dam.hotelsol.mejorada.model.Habitacion;
import es.dam.hotelsol.mejorada.model.Reserva;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador de la version B. Aplica las mejoras de usabilidad y accesibilidad
 * sin cambiar los datos, la tarea ni las reglas de exito.
 */
public class MainController {

    private static final String AYUDA_CAMPO =
            "Escribe un numero de tres cifras y pulsa Enter o el boton Buscar habitacion.";

    @FXML private javafx.scene.Node seccionInicio;
    @FXML private javafx.scene.Node seccionHabitaciones;
    @FXML private javafx.scene.Node seccionReservas;
    @FXML private javafx.scene.Node seccionUsuarios;
    @FXML private javafx.scene.Node seccionConfiguracion;

    @FXML private Button navInicio;
    @FXML private Button navHabitaciones;
    @FXML private Button navReservas;
    @FXML private Button navUsuarios;
    @FXML private Button navConfiguracion;

    @FXML private Label lblParticipante;
    @FXML private Label lblVersion;

    @FXML private Label lblTotalHabitaciones;
    @FXML private Label lblDisponibles;
    @FXML private Label lblOcupadas;
    @FXML private Label lblMantenimiento;

    @FXML private Label lblNumero;
    @FXML private TextField campoNumero;
    @FXML private Button botonBuscar;
    @FXML private Button botonLimpiar;
    @FXML private Label lblErrorCampo;
    @FXML private FlowPane panelSugerencias;
    @FXML private VBox panelAyuda;
    @FXML private VBox panelResultado;
    @FXML private Label lblIconoEstado;
    @FXML private Label lblTituloEstado;
    @FXML private Label lblTextoEstado;

    @FXML private VBox panelHabitacion;
    @FXML private Label lblNumeroHabitacion;
    @FXML private Label lblBadgeDisponibilidad;
    @FXML private Label lblEstadoHabitacion;
    @FXML private Label lblTipoHabitacion;
    @FXML private Label lblPlantaHabitacion;
    @FXML private Label lblCapacidadHabitacion;
    @FXML private Label lblPrecioHabitacion;
    @FXML private Label lblVistaHabitacion;
    @FXML private Label lblAccesibleHabitacion;
    @FXML private Button botonFinalizar;
    @FXML private Label lblAyudaFinalizar;

    @FXML private VBox panelCompletado;
    @FXML private Label lblResumenCompletado;
    @FXML private VBox panelHistorial;
    @FXML private VBox contenedorHistorial;
    @FXML private Label lblRegistro;

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
    private final List<String> historial = new ArrayList<>();
    private SesionPrueba sesion;

    @FXML
    public void initialize() {
        configurarAccesibilidad();
        configurarTablas();
        configurarEstadisticas();
        mostrarSeccion(seccionInicio, navInicio);

        lblIconoEstado.setAccessibleRole(AccessibleRole.NODE);
        panelResultado.setVisible(false);
        panelResultado.setManaged(false);
        panelHabitacion.setVisible(false);
        panelHabitacion.setManaged(false);
        panelCompletado.setVisible(false);
        panelCompletado.setManaged(false);
        panelHistorial.setVisible(false);
        panelHistorial.setManaged(false);
        panelSugerencias.setVisible(false);
        panelSugerencias.setManaged(false);
        lblErrorCampo.setVisible(false);
        lblErrorCampo.setManaged(false);
        lblRegistro.setText("");
        botonFinalizar.setDisable(true);
        lblAyudaFinalizar.setText("La opcion se activa al localizar la habitacion indicada en la tarea.");

        lblHotelNombre.setText(dao.getNombreHotel());
        lblHotelCategoria.setText(dao.getCategoria());
        lblHotelCiudad.setText(dao.getCiudad());
        lblHotelDireccion.setText(dao.getDireccion());
        lblHotelTelefono.setText(dao.getTelefono());
        lblHotelFecha.setText(dao.getFechaDatos());

        TextFormatter<String> soloTresDigitos = new TextFormatter<>(cambio ->
                cambio.getControlNewText().matches("\\d{0,3}") ? cambio : null);
        campoNumero.setTextFormatter(soloTresDigitos);
        campoNumero.setOnAction(e -> buscar());
        campoNumero.textProperty().addListener((obs, anterior, nuevo) -> {
            if (lblErrorCampo.isVisible()) {
                ocultarErrorCampo();
            }
        });
    }

    private void configurarAccesibilidad() {
        lblNumero.setLabelFor(campoNumero);
        campoNumero.setAccessibleText("Numero de habitacion");
        campoNumero.setAccessibleHelp(AYUDA_CAMPO);
        botonBuscar.setAccessibleText("Buscar habitacion");
        botonBuscar.setAccessibleHelp("Busca la habitacion cuyo numero esta escrito en el campo.");
        botonLimpiar.setAccessibleText("Limpiar el formulario de busqueda");
        botonFinalizar.setAccessibleText("He comprobado la disponibilidad");
        botonFinalizar.setAccessibleHelp("Finaliza la prueba despues de consultar el estado de la habitacion.");
    }

    public void iniciarSesion(SesionPrueba nuevaSesion) {
        this.sesion = nuevaSesion;
        lblParticipante.setText(nuevaSesion.getParticipante());
        lblVersion.setText("Version B");
    }

    public void configurarAtajos(Scene escena) {
        escena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN), this::irAlBuscador);
        escena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN), this::limpiar);
    }

    private void configurarEstadisticas() {
        lblTotalHabitaciones.setText(String.valueOf(dao.getHabitaciones().size()));
        lblDisponibles.setText(String.valueOf(dao.contarPorEstado("disponible")));
        lblOcupadas.setText(String.valueOf(dao.contarPorEstado("ocupada")));
        lblMantenimiento.setText(String.valueOf(dao.contarPorEstado("mantenimiento")));
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
    private void mostrarHabitaciones() {
        mostrarSeccion(seccionHabitaciones, navHabitaciones);
        if (sesion != null && !sesion.isFinalizada()) {
            Platform.runLater(campoNumero::requestFocus);
        }
    }

    @FXML
    private void mostrarReservas() { mostrarSeccion(seccionReservas, navReservas); }

    @FXML
    private void mostrarUsuarios() { mostrarSeccion(seccionUsuarios, navUsuarios); }

    @FXML
    private void mostrarConfiguracion() { mostrarSeccion(seccionConfiguracion, navConfiguracion); }

    @FXML
    private void irAlBuscador() {
        mostrarSeccion(seccionHabitaciones, navHabitaciones);
        Platform.runLater(campoNumero::requestFocus);
    }

    private void mostrarSeccion(javafx.scene.Node seccion, Button botonActivo) {
        List<javafx.scene.Node> secciones = List.of(seccionInicio, seccionHabitaciones, seccionReservas,
                seccionUsuarios, seccionConfiguracion);
        for (javafx.scene.Node s : secciones) {
            boolean activa = s == seccion;
            s.setVisible(activa);
            s.setManaged(activa);
        }
        for (Button b : List.of(navInicio, navHabitaciones, navReservas, navUsuarios, navConfiguracion)) {
            b.getStyleClass().remove("activo");
            b.setAccessibleHelp("Abrir la seccion " + b.getText());
        }
        if (botonActivo != null) {
            botonActivo.getStyleClass().add("activo");
            botonActivo.setAccessibleHelp("Seccion actual");
        }
    }

    @FXML
    private void buscar() {
        if (sesion == null || sesion.isFinalizada()) {
            return;
        }
        String consulta = campoNumero.getText() == null ? "" : campoNumero.getText().trim();
        if (consulta.isEmpty()) {
            sesion.registrarBusqueda("", null);
            mostrarEstado("error", "!", "Escribe un numero de habitacion",
                    "El campo esta vacio. Introduce un numero de tres cifras para poder buscar.");
            mostrarErrorCampo("Introduce un numero antes de buscar.");
            mostrarSugerencias(0);
            registrarHistorial("(vacio)", "sin busqueda");
            campoNumero.requestFocus();
            return;
        }

        Habitacion habitacion = dao.buscar(consulta);
        boolean acierto = sesion.registrarBusqueda(consulta, habitacion);

        if (habitacion == null) {
            ocultarFicha();
            mostrarEstado("error", "!", "No existe la habitacion " + consulta,
                    "Ese numero no corresponde a ninguna habitacion del hotel. Revisa el numero e intentalo de nuevo.");
            mostrarErrorCampo("No existe ninguna habitacion con el numero " + consulta + ".");
            mostrarSugerencias(Integer.parseInt(consulta));
            registrarHistorial(consulta, "sin resultado");
            campoNumero.selectAll();
            campoNumero.requestFocus();
            return;
        }

        ocultarErrorCampo();
        mostrarSugerencias(-1);
        mostrarFicha(habitacion);

        if (acierto) {
            mostrarEstado("exito", "OK", "Habitacion " + habitacion.getNumero() + " localizada",
                    "Consulta el estado en la ficha y pulsa el boton He comprobado la disponibilidad.");
            botonFinalizar.setDisable(false);
            lblAyudaFinalizar.setText("Cuando hayas comprobado el estado, finaliza la prueba.");
            campoNumero.setDisable(true);
            botonBuscar.setDisable(true);
            botonLimpiar.setDisable(true);
            registrarHistorial(consulta, "localizada");
            Platform.runLater(botonFinalizar::requestFocus);
        } else {
            mostrarEstado("aviso", "i", "Habitacion " + habitacion.getNumero() + ": no es la indicada",
                    "La habitacion existe, pero la tarea pide otro numero. Revisa el numero y vuelve a buscar.");
            botonFinalizar.setDisable(true);
            lblAyudaFinalizar.setText("Busca la habitacion indicada en la tarea para poder finalizar.");
            registrarHistorial(consulta, "no es la indicada");
            campoNumero.selectAll();
            campoNumero.requestFocus();
        }
    }

    @FXML
    private void limpiar() {
        if (sesion == null || sesion.isFinalizada() || campoNumero.isDisabled()) {
            return;
        }
        campoNumero.clear();
        ocultarErrorCampo();
        panelSugerencias.setVisible(false);
        panelSugerencias.setManaged(false);
        panelResultado.setVisible(false);
        panelResultado.setManaged(false);
        panelAyuda.setVisible(true);
        panelAyuda.setManaged(true);
        ocultarFicha();
        botonFinalizar.setDisable(true);
        lblAyudaFinalizar.setText("La opcion se activa al localizar la habitacion indicada en la tarea.");
        campoNumero.requestFocus();
    }

    private void mostrarFicha(Habitacion habitacion) {
        lblNumeroHabitacion.setText(habitacion.getNumero());
        lblEstadoHabitacion.setText(habitacion.getEstadoLegible());
        lblTipoHabitacion.setText(habitacion.getTipo());
        lblPlantaHabitacion.setText(habitacion.getPlanta());
        lblCapacidadHabitacion.setText(habitacion.getCapacidad() + " personas");
        lblPrecioHabitacion.setText(habitacion.getPrecioLegible());
        lblVistaHabitacion.setText(habitacion.getVista());
        lblAccesibleHabitacion.setText(habitacion.isAccesible() ? "Si" : "No");

        boolean disponible = habitacion.estaDisponible();
        lblBadgeDisponibilidad.getStyleClass().removeAll("badge-disponible", "badge-no-disponible");
        if (disponible) {
            lblBadgeDisponibilidad.setText("Disponible");
            lblBadgeDisponibilidad.getStyleClass().add("badge-disponible");
            lblBadgeDisponibilidad.setAccessibleText("Estado: disponible");
        } else {
            String estado = "mantenimiento".equalsIgnoreCase(habitacion.getEstado())
                    ? "En mantenimiento" : "Ocupada";
            lblBadgeDisponibilidad.setText("No disponible - " + estado);
            lblBadgeDisponibilidad.getStyleClass().add("badge-no-disponible");
            lblBadgeDisponibilidad.setAccessibleText("Estado: " + estado + ". No disponible.");
        }
        panelHabitacion.setVisible(true);
        panelHabitacion.setManaged(true);
        animar(panelHabitacion);
    }

    private void ocultarFicha() {
        panelHabitacion.setVisible(false);
        panelHabitacion.setManaged(false);
    }

    private void mostrarEstado(String tipo, String icono, String titulo, String texto) {
        panelAyuda.setVisible(false);
        panelAyuda.setManaged(false);
        panelResultado.getStyleClass().removeAll("estado-error", "estado-aviso", "estado-exito", "estado-info");
        panelResultado.getStyleClass().add("estado-" + tipo);
        lblIconoEstado.setText(icono);
        lblTituloEstado.setText(titulo);
        lblTextoEstado.setText(texto);
        lblTituloEstado.setAccessibleText(titulo);
        lblTextoEstado.setAccessibleText(texto);
        panelResultado.setVisible(true);
        panelResultado.setManaged(true);
        Platform.runLater(() -> {
            lblTituloEstado.notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
            lblTextoEstado.notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        });
        animar(panelResultado);
    }

    private void animar(javafx.scene.Node nodo) {
        FadeTransition fundido = new FadeTransition(Duration.millis(140), nodo);
        fundido.setFromValue(0.35);
        fundido.setToValue(1.0);
        fundido.play();
    }

    private void mostrarErrorCampo(String mensaje) {
        lblErrorCampo.setText(mensaje);
        lblErrorCampo.setVisible(true);
        lblErrorCampo.setManaged(true);
        campoNumero.setAccessibleHelp("Error: " + mensaje + " " + AYUDA_CAMPO);
        lblErrorCampo.notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
    }

    private void ocultarErrorCampo() {
        lblErrorCampo.setVisible(false);
        lblErrorCampo.setManaged(false);
        campoNumero.setAccessibleHelp(AYUDA_CAMPO);
    }

    /**
     * Sugerencias de numeros existentes cercanos. Se excluye la habitacion
     * objetivo para no resolver la tarea automaticamente.
     */
    private void mostrarSugerencias(int numeroBase) {
        panelSugerencias.getChildren().clear();
        if (numeroBase < 0) {
            panelSugerencias.setVisible(false);
            panelSugerencias.setManaged(false);
            return;
        }
        int base = numeroBase == 0 ? 101 : numeroBase;
        List<Habitacion> cercanas = dao.getHabitaciones().stream()
                .filter(h -> !h.getNumero().equals(SesionPrueba.HABITACION_OBJETIVO))
                .filter(h -> !h.getNumero().equals(String.valueOf(numeroBase)))
                .sorted(Comparator.comparingInt(h -> Math.abs(Integer.parseInt(h.getNumero()) - base)))
                .limit(3)
                .toList();
        Label etiqueta = new Label("Prueba con:");
        etiqueta.getStyleClass().add("texto-ayuda");
        panelSugerencias.getChildren().add(etiqueta);
        for (Habitacion h : cercanas) {
            Button chip = new Button(h.getNumero());
            chip.getStyleClass().add("chip");
            chip.setAccessibleText("Probar con la habitacion " + h.getNumero());
            chip.setOnAction(e -> {
                campoNumero.setText(h.getNumero());
                buscar();
            });
            panelSugerencias.getChildren().add(chip);
        }
        panelSugerencias.setVisible(true);
        panelSugerencias.setManaged(true);
    }

    private void registrarHistorial(String consulta, String resultado) {
        historial.add(0, consulta + " - " + resultado);
        while (historial.size() > 4) {
            historial.remove(historial.size() - 1);
        }
        contenedorHistorial.getChildren().clear();
        for (String linea : historial) {
            Label item = new Label(linea);
            item.getStyleClass().add("historial-item");
            contenedorHistorial.getChildren().add(item);
        }
        panelHistorial.setVisible(true);
        panelHistorial.setManaged(true);
    }

    @FXML
    private void finalizar() {
        if (sesion == null || sesion.isFinalizada()) {
            return;
        }
        if (!sesion.intentarFinalizar()) {
            mostrarEstado("error", "!", "Todavia no puedes finalizar",
                    "Busca la habitacion indicada en la tarea antes de finalizar la prueba.");
            return;
        }
        RegistroPrueba registro = sesion.completar();
        if (registro == null) {
            return;
        }
        botonFinalizar.setDisable(true);
        campoNumero.setDisable(true);
        botonBuscar.setDisable(true);
        botonLimpiar.setDisable(true);
        EnvioMetricas.Resultado envio = EnvioMetricas.enviar(registro);
        lblRegistro.setText(envio.mensaje());

        panelHabitacion.setVisible(false);
        panelHabitacion.setManaged(false);
        panelResultado.setVisible(false);
        panelResultado.setManaged(false);
        panelCompletado.setVisible(true);
        panelCompletado.setManaged(true);
        lblResumenCompletado.setText(String.format(Locale.forLanguageTag("es-ES"),
                "Tiempo: %.2f s    Busquedas: %d    Errores: %d",
                registro.duracionSeg, registro.busquedas, registro.errores));
        animar(panelCompletado);
        Platform.runLater(panelCompletado::requestFocus);
    }

    @FXML
    private void cerrarAplicacion() {
        Stage ventana = (Stage) panelCompletado.getScene().getWindow();
        ventana.close();
    }

    /** Confirmacion antes de cerrar: evita perder una prueba en curso por accidente. */
    public boolean puedeCerrar() {
        if (sesion == null || sesion.isFinalizada()) {
            return true;
        }
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.initOwner(panelCompletado.getScene().getWindow());
        alerta.setTitle("Abandonar la prueba");
        alerta.setHeaderText("La prueba aun no ha terminado");
        alerta.setContentText("Si cierras ahora, se registrara como no completada. "
                + "Puedes seguir con la prueba o abandonarla.");
        ButtonType seguir = new ButtonType("Seguir con la prueba", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType abandonar = new ButtonType("Abandonar", ButtonBar.ButtonData.OK_DONE);
        alerta.getButtonTypes().setAll(seguir, abandonar);
        alerta.getDialogPane().getStylesheets().add(
                App.class.getResource("/css/mejorada.css").toExternalForm());
        Optional<ButtonType> respuesta = alerta.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == abandonar) {
            abandonarSiProcede();
            return true;
        }
        return false;
    }

    /** Si la ventana se cierra con la prueba en curso, se registra el abandono. */
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
