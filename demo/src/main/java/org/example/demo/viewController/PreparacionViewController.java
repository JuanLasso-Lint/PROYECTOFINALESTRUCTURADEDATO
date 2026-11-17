package org.example.demo.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.pro_final_ed.controller.ModelFactoryController;
import org.example.pro_final_ed.model.Estructuras.ListaDoble;
import org.example.pro_final_ed.model.TareaLimpieza;

public class PreparacionViewController {

    @FXML private TextField txtOrigenLimpieza;
    @FXML private TextArea txtColaLimpieza;

    private final ModelFactoryController mfc = ModelFactoryController.getInstance();

    @FXML
    public void initialize() {
        actualizarCola();
    }

    @FXML
    public void onAtenderLimpieza() {
        try {
            String origen = txtOrigenLimpieza.getText().trim();
            ListaDoble<String> ruta = mfc.atenderLimpieza(origen);
            actualizarCola();
            new Alert(Alert.AlertType.INFORMATION, "Limpieza completada. Ruta seguida (Dijkstra): " + ruta).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void actualizarCola() {
        StringBuilder sb = new StringBuilder("=== COLA DE PREPARACIÓN / ASEO (PRIORIDAD) ===\n");
        if (mfc.getHotel().getColaLimpieza().estaVacia()) {
            sb.append("No hay tareas pendientes en cola.\n");
        } else {
            for (TareaLimpieza t : mfc.getHotel().getColaLimpieza()) {
                sb.append("• ").append(t.toString()).append("\n");
            }
        }
        txtColaLimpieza.setText(sb.toString());
    }
}
