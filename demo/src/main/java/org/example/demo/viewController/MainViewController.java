package org.example.demo.viewController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.example.pro_final_ed.controller.ModelFactoryController;

import java.io.IOException;

public class MainViewController {

    @FXML private StackPane contentArea;
    @FXML private Label lblTurnoActual;
    @FXML private Label lblEstadoCarga;
    @FXML private Button btnAtras;

    private final ModelFactoryController mfc = ModelFactoryController.getInstance();

    @FXML
    public void initialize() {
        actualizarBarraSuperior();
        cargarVista("inicio-view.fxml", false);
    }

    private void actualizarBarraSuperior() {
        lblTurnoActual.setText("Recepcionista en turno: " + mfc.turnoActual());
        lblEstadoCarga.setText("Aptos activos: " + mfc.getHotel().getCatalogoApartamentos().getTam());
        btnAtras.setDisable(!mfc.puedeRegresar());
    }

    private void cargarVista(String fxmlName, boolean registrarEnPila) {
        try {
            if (registrarEnPila) mfc.registrarNavegacion(fxmlName);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/pro_final_ed/" + fxmlName));
            Node vista = loader.load();
            contentArea.getChildren().setAll(vista);
            actualizarBarraSuperior();
        } catch (IOException e) {
            mostrarAlerta("Error de Navegación", "No se pudo cargar la vista " + fxmlName + ": " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onCargarDatosIniciales() {
        try {
            mfc.cargarEscenarioInicial();
            actualizarBarraSuperior();
            cargarVista("inicio-view.fxml", false);
            mostrarAlerta("Carga Exitosa", "Escenario de prueba recargado correctamente.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            mostrarAlerta("Error al Cargar", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onRotarTurno() {
        String nuevo = mfc.rotarTurno();
        actualizarBarraSuperior();
        mostrarAlerta("Rotación de Turno", "Nuevo recepcionista asignado: " + nuevo, Alert.AlertType.INFORMATION);
    }

    @FXML
    public void onRegresarAtras() {
        String anterior = mfc.regresarVistaAnterior();
        if (anterior != null) cargarVista(anterior, false);
    }

    @FXML public void irInicio() { cargarVista("inicio-view.fxml", true); }
    @FXML public void irReservas() { cargarVista("reservas-view.fxml", true); }
    @FXML public void irCalendario() { cargarVista("calendario-view.fxml", true); }
    @FXML public void irUsuario() { cargarVista("usuario-view.fxml", true); }
    @FXML public void irEmpleados() { cargarVista("empleados-view.fxml", true); }
    @FXML public void irPreparacion() { cargarVista("preparacion-view.fxml", true); }
    @FXML public void irMapa() { cargarVista("mapa-view.fxml", true); }
    @FXML public void irEstadisticas() { cargarVista("estadisticas-view.fxml", true); }

    private void mostrarAlerta(String titulo, String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
