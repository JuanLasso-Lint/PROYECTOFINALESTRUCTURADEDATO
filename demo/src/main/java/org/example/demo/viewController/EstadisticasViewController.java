package org.example.demo.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.example.demo.controller.ModelFactoryController;

public class EstadisticasViewController {

    @FXML private TextArea txtReporteEstadisticas;
    private final ModelFactoryController mfc = ModelFactoryController.getInstance();

    @FXML
    public void initialize() {
        generarReporte();
    }

    @FXML
    public void generarReporte() {
        txtReporteEstadisticas.setText(mfc.obtenerEstadisticas());
    }
}
