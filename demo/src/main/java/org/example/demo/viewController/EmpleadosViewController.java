package org.example.pro_final_ed.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.pro_final_ed.controller.ModelFactoryController;
import org.example.pro_final_ed.model.Empleado;
import org.example.pro_final_ed.model.Temporada;

public class EmpleadosViewController {

    @FXML private TextField txtNombreEmp;
    @FXML private TextField txtCodigoEmp;
    @FXML private ComboBox<String> cmbCargoEmp;

    @FXML private TextField txtNombreTemp;
    @FXML private DatePicker dpInicioTemp;
    @FXML private DatePicker dpFinTemp;
    @FXML private TextField txtMultTemp;

    @FXML private TextArea txtListaEmpleados;

    private final ModelFactoryController mfc = ModelFactoryController.getInstance();

    @FXML
    public void initialize() {
        cmbCargoEmp.getItems().setAll("Recepcionista", "Limpieza", "Mantenimiento", "Administrador");
        cmbCargoEmp.setValue("Recepcionista");
        listarEmpleados();
    }

    @FXML
    public void onRegistrarEmpleado() {
        try {
            String nombre = txtNombreEmp.getText().trim();
            String codigo = txtCodigoEmp.getText().trim();
            String cargo = cmbCargoEmp.getValue();

            if (nombre.isEmpty() || codigo.isEmpty() || cargo == null) {
                throw new IllegalArgumentException("Todos los campos del trabajador son obligatorios.");
            }

            mfc.registrarEmpleado(nombre, codigo, cargo);
            txtNombreEmp.clear();
            txtCodigoEmp.clear();
            listarEmpleados();
            new Alert(Alert.AlertType.INFORMATION, "Trabajador " + nombre + " registrado como " + cargo + ".").showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error al registrar trabajador: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onRegistrarTemporada() {
        try {
            String nombre = txtNombreTemp.getText().trim();
            if (nombre.isEmpty() || dpInicioTemp.getValue() == null || dpFinTemp.getValue() == null) {
                throw new IllegalArgumentException("Completa nombre, fecha inicio y fecha fin de la temporada.");
            }
            double mult = Double.parseDouble(txtMultTemp.getText().trim());
            mfc.registrarTemporada(nombre, dpInicioTemp.getValue(), dpFinTemp.getValue(), mult);
            txtNombreTemp.clear();
            txtMultTemp.clear();
            listarEmpleados();
            new Alert(Alert.AlertType.INFORMATION, "Temporada " + nombre + " registrada.").showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error al registrar temporada: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onRotarTurnoEmp() {
        String nuevo = mfc.rotarTurno();
        listarEmpleados();
        new Alert(Alert.AlertType.INFORMATION, "Turno rotado. Recepcionista actual: " + nuevo).showAndWait();
    }

    @FXML
    public void listarEmpleados() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TURNO ACTUAL EN RECEPCIÓN ===\n");
        sb.append("• En turno: ").append(mfc.turnoActual()).append("\n\n");
        sb.append("=== PERSONAL REGISTRADO EN EL HOTEL ===\n");

        for (Empleado emp : mfc.getHotel().getEmpleados()) {
            sb.append(String.format("• [%s] %s — Cargo: %s\n",
                    emp.getCodigo(), emp.getNombre(), emp.getCargo()));
        }
        sb.append("\n=== TEMPORADAS CONFIGURADAS ===\n");
        for (Temporada t : mfc.getHotel().getTemporadas()) {
            sb.append("• ").append(t.toString()).append("\n");
        }
        txtListaEmpleados.setText(sb.toString());
    }
}
