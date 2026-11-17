package org.example.pro_final_ed.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.example.pro_final_ed.controller.ModelFactoryController;

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
