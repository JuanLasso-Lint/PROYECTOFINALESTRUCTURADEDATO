package org.example.pro_final_ed.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import org.example.pro_final_ed.controller.ModelFactoryController;
import org.example.pro_final_ed.model.Apartamento;

import java.time.LocalDate;

public class CalendarioViewController {

    @FXML private DatePicker dpFechaInicio;
    @FXML private TextArea txtMatrizCalendario;

    private final ModelFactoryController mfc = ModelFactoryController.getInstance();

    @FXML
    public void initialize() {
        dpFechaInicio.setValue(LocalDate.now());
        generarMatriz();
    }

    @FXML
    public void generarMatriz() {
        LocalDate base = dpFechaInicio.getValue() != null ? dpFechaInicio.getValue() : LocalDate.now();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-12s", "APTO"));
        for (int d = 0; d < 10; d++) {
            sb.append(String.format("%-12s", base.plusDays(d).toString().substring(5)));
        }
        sb.append("\n").append("=".repeat(132)).append("\n");

        for (Apartamento a : mfc.getHotel().getCatalogoApartamentos()) {
            sb.append(String.format("%-12s", a.getCodigo()));
            for (int d = 0; d < 10; d++) {
                LocalDate dia = base.plusDays(d);
                boolean libre = mfc.getHotel().estaDisponible(a, dia, dia.plusDays(1));
                sb.append(String.format("%-12s", libre ? "[ LIBRE ]" : "[OCUPADO]"));
            }
            sb.append("\n");
        }
        txtMatrizCalendario.setText(sb.toString());
    }
}
