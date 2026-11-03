package es.dam.hotelsol.mejorada;

import es.dam.hotelsol.mejorada.controller.MainController;
import es.dam.hotelsol.mejorada.metrics.ApiCliente;
import es.dam.hotelsol.mejorada.metrics.SesionPrueba;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Version B: interfaz mejorada. La tarea, los datos y las reglas de busqueda
 * son identicos a la version A; solo cambia la interfaz y la ayuda al usuario.
 */
public class App extends Application {

    private MainController controlador;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/mejorada.fxml"));
        Parent raiz = loader.load();
        controlador = loader.getController();

        Scene escena = new Scene(raiz, 1180, 780);
        escena.getStylesheets().add(App.class.getResource("/css/mejorada.css").toExternalForm());
        stage.setScene(escena);
        stage.setTitle("Hotel Sol - Buscar habitacion (Version B)");
        stage.setMinWidth(1000);
        stage.setMinHeight(660);
        controlador.configurarAtajos(escena);
        stage.show();

        Platform.runLater(() -> {
            String id = pedirIdentificador(stage);
            if (id == null) {
                Platform.exit();
                return;
            }
            String orden = ApiCliente.obtenerOCrearOrden(id);
            controlador.iniciarSesion(new SesionPrueba(id, "B", orden));
        });

        stage.setOnCloseRequest(evento -> {
            if (!controlador.puedeCerrar()) {
                evento.consume();
            }
        });
    }

    private String pedirIdentificador(Stage propietario) {
        Dialog<String> dialogo = new Dialog<>();
        dialogo.initOwner(propietario);
        dialogo.setTitle("Iniciar prueba");
        dialogo.getDialogPane().getStylesheets().add(
                App.class.getResource("/css/mejorada.css").toExternalForm());
        dialogo.getDialogPane().getStyleClass().add("dialogo-prueba");

        ButtonType comenzar = new ButtonType("Comenzar prueba", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(comenzar, ButtonType.CANCEL);

        Label titulo = new Label("Bienvenido/a a la prueba de usabilidad");
        titulo.getStyleClass().add("dialogo-titulo");
        Label explicacion = new Label(
                "Introduce el código anónimo que aparece en la web. No uses tu nombre ni datos personales.");
        explicacion.setWrapText(true);
        explicacion.getStyleClass().add("dialogo-texto");

        TextField campo = new TextField();
        campo.setPromptText("Ejemplo: P01");
        campo.getStyleClass().add("campo-grande");
        campo.setAccessibleText("Código anónimo de participante");

        Label error = new Label();
        error.getStyleClass().add("error-campo");
        error.setWrapText(true);
        error.setVisible(false);
        error.setManaged(false);

        VBox contenido = new VBox(10, titulo, explicacion, new Label("Código de participante:"), campo, error);
        contenido.setPadding(new Insets(12, 4, 4, 4));
        contenido.setPrefWidth(460);
        dialogo.getDialogPane().setContent(contenido);

        Button botonComenzar = (Button) dialogo.getDialogPane().lookupButton(comenzar);
        botonComenzar.addEventFilter(ActionEvent.ACTION, evento -> {
            String texto = campo.getText() == null ? "" : campo.getText().trim();
            if (!texto.matches("[A-Za-z0-9_-]{2,20}")) {
                error.setText("Usa entre 2 y 20 caracteres: letras, números, guion o guion bajo.");
                error.setVisible(true);
                error.setManaged(true);
                campo.requestFocus();
                evento.consume();
            }
        });
        dialogo.setResultConverter(tipo -> tipo == comenzar ? campo.getText().trim() : null);
        Platform.runLater(campo::requestFocus);
        return dialogo.showAndWait().orElse(null);
    }

    @Override
    public void stop() {
        if (controlador != null) {
            controlador.abandonarSiProcede();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
