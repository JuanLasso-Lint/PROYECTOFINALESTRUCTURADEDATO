package org.example.demo.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.pro_final_ed.controller.ModelFactoryController;
import org.example.pro_final_ed.model.*;
import org.example.pro_final_ed.model.Estructuras.ListaDoble;

import java.time.LocalDate;

public class ReservasViewController {

    @FXML private DatePicker dpEntrada;
    @FXML private DatePicker dpSalida;
    @FXML private Spinner<Integer> spPersonas;
    @FXML private ComboBox<String> cmbCriterio;
    @FXML private ListView<Apartamento> lvDisponibles;

    @FXML private TextField txtDocHuesped;
    @FXML private TextField txtDocsAcompanantes;
    @FXML private ComboBox<CanalReserva> cmbCanal;
    @FXML private TextArea txtLogReservas;

    private final ModelFactoryController mfc = ModelFactoryController.getInstance();

    @FXML
    public void initialize() {
        dpEntrada.setValue(LocalDate.now().plusDays(5));
        dpSalida.setValue(LocalDate.now().plusDays(8));
        spPersonas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        cmbCriterio.getItems().setAll("DORMITORIOS", "PRECIO", "CAPACIDAD");
        cmbCriterio.setValue("DORMITORIOS");
        cmbCanal.getItems().setAll(CanalReserva.values());
        cmbCanal.setValue(CanalReserva.DIRECTA);
        actualizarLog();
    }

    @FXML
    public void onBuscarDisponibles() {
        lvDisponibles.getItems().clear();
        ListaDoble<Apartamento> lista = mfc.buscarDisponibles(dpEntrada.getValue(), dpSalida.getValue(), spPersonas.getValue(), cmbCriterio.getValue());
        for (Apartamento a : lista) lvDisponibles.getItems().add(a);
    }

    @FXML
    public void onAsignarGrupoGrande() {
        lvDisponibles.getItems().clear();
        ListaDoble<Apartamento> combo = mfc.asignarGrupoGrande(dpEntrada.getValue(), dpSalida.getValue(), spPersonas.getValue());
        if (combo.estaVacia()) {
            new Alert(Alert.AlertType.WARNING, "No hay combinación de apartamentos disponible para " + spPersonas.getValue() + " personas.").showAndWait();
        } else {
            for (Apartamento a : combo) lvDisponibles.getItems().add(a);
        }
    }

    @FXML
    public void onConfirmarReserva() {
        try {
            Apartamento sel = lvDisponibles.getSelectionModel().getSelectedItem();
            if (sel == null) throw new IllegalArgumentException("Selecciona un apartamento de la lista.");
            Huesped h = mfc.getHotel().buscarHuesped(txtDocHuesped.getText().trim());
            if (h == null) throw new IllegalArgumentException("No existe un huésped titular con ese documento.");

            Reserva r = new Reserva(h, sel, dpEntrada.getValue(), dpSalida.getValue(), cmbCanal.getValue());
            String acompRaw = txtDocsAcompanantes.getText().trim();
            if (!acompRaw.isEmpty()) {
                for (String docAc : acompRaw.split(",")) {
                    Huesped ac = mfc.getHotel().buscarHuesped(docAc.trim());
                    if (ac == null) {
                        throw new IllegalArgumentException("No existe el acompañante con documento: " + docAc.trim());
                    }
                    r.agregarAcompanante(ac);
                }
            }

            double total = mfc.cotizar(r);
            r.registrarMovimiento("Tarifa estancia (" + r.getCantidadNoches() + " noches, " + r.getTotalPersonas() + " pers.)", total, true);
            mfc.crearReserva(r);
            txtDocsAcompanantes.clear();
            actualizarLog();
            new Alert(Alert.AlertType.INFORMATION, "Reserva #" + r.getId() + " registrada (" + r.getEstado() + "). Personas: " + r.getTotalPersonas() + " | Total cotizado: $" + String.format("%.0f", total)).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onProcesarExterna() {
        boolean ok = mfc.procesarExterna();
        actualizarLog();
        new Alert(Alert.AlertType.INFORMATION, ok ? "Solicitud externa aprobada y confirmada." : "No había solicitudes externas o hubo conflicto de fechas.").showAndWait();
    }

    @FXML
    public void onCancelarVencidas() {
        int n = mfc.cancelarVencidas();
        actualizarLog();
        new Alert(Alert.AlertType.INFORMATION, "Reservas pendientes vencidas canceladas: " + n).showAndWait();
    }

    private void actualizarLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SOLICITUDES EN COLA CANAL EXTERNO (FIFO): ").append(mfc.getHotel().getSolicitudesExternas().getTam()).append(" ===\n\n");
        sb.append("=== HISTORIAL DE RESERVAS ===\n");
        for (Reserva r : mfc.getHotel().getHistorialGlobalReservas()) {
            sb.append(String.format("#%d | Apto %s | Canal: %s | Titular: %s %s (%d pers.) | %s a %s | [%s] | Pagado Neto: $%,.0f\n",
                    r.getId(), r.getApartamentoAsignado().getCodigo(), r.getCanal(),
                    r.getTitular().getNombre(), r.getTitular().getApellido(), r.getTotalPersonas(),
                    r.getFechaEntrada(), r.getFechaSalida(), r.getEstado(), r.calcularTotalPagadoNeto()));
        }
        txtLogReservas.setText(sb.toString());
    }
}
