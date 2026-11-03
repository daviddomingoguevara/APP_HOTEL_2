package es.dam.hotelsol.basica;

import es.dam.hotelsol.basica.controller.MainController;
import es.dam.hotelsol.basica.metrics.ApiCliente;
import es.dam.hotelsol.basica.metrics.SesionPrueba;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Version A: interfaz basica de gestion hotelera.
 * La prueba se inicia cuando el participante introduce su codigo anonimo.
 */
public class App extends Application {

    private MainController controlador;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/basica.fxml"));
        Parent raiz = loader.load();
        controlador = loader.getController();

        Scene escena = new Scene(raiz, 1120, 720);
        escena.getStylesheets().add(App.class.getResource("/css/basica.css").toExternalForm());
        stage.setScene(escena);
        stage.setTitle("Hotel Sol - Gestion hotelera (Version A)");
        stage.setMinWidth(940);
        stage.setMinHeight(620);
        stage.show();

        Platform.runLater(() -> {
            String id = pedirIdentificador(stage);
            if (id == null) {
                Platform.exit();
                return;
            }
            String orden = ApiCliente.obtenerOCrearOrden(id);
            controlador.iniciarSesion(new SesionPrueba(id, "A", orden));
        });
    }

    private String pedirIdentificador(Stage propietario) {
        while (true) {
            TextInputDialog dialogo = new TextInputDialog("anonimo");
            dialogo.initOwner(propietario);
            dialogo.setTitle("Iniciar prueba");
            dialogo.setHeaderText("Introduce el codigo anonimo de participante");
            dialogo.setContentText("Codigo (el que aparece en la web):");
            Optional<String> respuesta = dialogo.showAndWait();
            if (respuesta.isEmpty()) {
                return null;
            }
            String id = respuesta.get().trim();
            if (id.matches("[A-Za-z0-9_-]{2,20}")) {
                return id;
            }
            Alert aviso = new Alert(Alert.AlertType.ERROR);
            aviso.initOwner(propietario);
            aviso.setTitle("Codigo no valido");
            aviso.setHeaderText(null);
            aviso.setContentText("Usa entre 2 y 20 caracteres: letras, numeros, guion o guion bajo.");
            aviso.showAndWait();
        }
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
