package org.example.pro_final_ed.viewController;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.pro_final_ed.controller.ModelFactoryController;
import org.example.pro_final_ed.model.Apartamento;
import org.example.pro_final_ed.model.EstadoOperativo;
import org.example.pro_final_ed.model.Reserva;

import java.time.LocalTime;

public class InicioViewController {

    @FXML private FlowPane flowApartamentos;
    @FXML private ComboBox<String> cmbAptoEstado;
    @FXML private ComboBox<EstadoOperativo> cmbNuevoEstado;
    @FXML private TextField txtMotivo;
    @FXML private TextField txtNuevaTarifaApto;
    @FXML private TextArea txtDetalleApto;

    @FXML private TextField txtRegCodigo;
    @FXML private TextField txtRegNombre;
    @FXML private TextField txtRegBloque;
    @FXML private TextField txtRegPiso;
    @FXML private TextField txtRegCap;
    @FXML private TextField txtRegDorm;
    @FXML private TextField txtRegCarac;
    @FXML private TextField txtRegTarifa;

    private final ModelFactoryController mfc = ModelFactoryController.getInstance();

    @FXML
    public void initialize() {
        cmbNuevoEstado.getItems().setAll(EstadoOperativo.values());
        refrescarTablero();
    }

    @FXML
    public void onRegistrarApartamento() {
        try {
            String cod = txtRegCodigo.getText().trim();
            String nom = txtRegNombre.getText().trim();
            int bloque = txtRegBloque.getText().trim().isEmpty() ? 1 : Integer.parseInt(txtRegBloque.getText().trim());
            int piso = Integer.parseInt(txtRegPiso.getText().trim());
            int cap = Integer.parseInt(txtRegCap.getText().trim());
            int dorm = Integer.parseInt(txtRegDorm.getText().trim());
            String carac = txtRegCarac.getText().trim();
            double tarifa = Double.parseDouble(txtRegTarifa.getText().trim());

            if (cod.isEmpty() || nom.isEmpty()) {
                throw new IllegalArgumentException("Código y nombre del apartamento son obligatorios.");
            }

            mfc.registrarApartamento(cod, nom, piso, cap, dorm, bloque, carac, tarifa);
            txtRegCodigo.clear();
            txtRegNombre.clear();
            txtRegBloque.clear();
            txtRegPiso.clear();
            txtRegCap.clear();
            txtRegDorm.clear();
            txtRegCarac.clear();
            txtRegTarifa.clear();
            refrescarTablero();
            new Alert(Alert.AlertType.INFORMATION, "Apartamento " + cod + " registrado correctamente.").showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error al registrar apartamento: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void refrescarTablero() {
        flowApartamentos.getChildren().clear();
        cmbAptoEstado.getItems().clear();

        if (mfc.getHotel().getCatalogoApartamentos().getTam() == 0) {
            Label vacio = new Label("No hay apartamentos cargados.");
            vacio.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
            flowApartamentos.getChildren().add(vacio);
            return;
        }

        for (Apartamento apto : mfc.getHotel().getCatalogoApartamentos()) {
            cmbAptoEstado.getItems().add(apto.getCodigo());

            VBox card = new VBox(6);
            card.setPrefWidth(215);
            card.setPadding(new Insets(12));
            String colorFondo = switch (apto.getEstadoOperativo()) {
                case PREPARADO -> "#d4edda";
                case OCUPADO -> "#cce5ff";
                case PENDENTE_DE_PREPARACION, EN_PREPARACION -> "#fff3cd";
                case FUERA_DE_SERVICO -> "#f8d7da";
            };
            String colorBorde = switch (apto.getEstadoOperativo()) {
                case PREPARADO -> "#28a745";
                case OCUPADO -> "#007bff";
                case PENDENTE_DE_PREPARACION, EN_PREPARACION -> "#ffc107";
                case FUERA_DE_SERVICO -> "#dc3545";
            };
            card.setStyle("-fx-background-color: " + colorFondo + "; -fx-border-color: " + colorBorde + "; -fx-border-width: 2; -fx-background-radius: 8; -fx-border-radius: 8;");

            Label lblCod = new Label("Apto " + apto.getCodigo() + " - " + apto.getNombre());
            lblCod.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            Label lblEst = new Label("Estado: " + apto.getEstadoOperativo());
            lblEst.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
            Label lblInfo = new Label("Bloque " + apto.getBloque() + " | Piso " + apto.getNumPiso() + " | $" + String.format("%.0f", apto.getTarifaPorPersona()));

            Button btnVer = new Button("Inspeccionar");
            btnVer.setOnAction(e -> mostrarDetalle(apto));

            card.getChildren().addAll(lblCod, lblEst, lblInfo, btnVer);
            flowApartamentos.getChildren().add(card);
        }
    }

    private void mostrarDetalle(Apartamento apto) {
        cmbAptoEstado.setValue(apto.getCodigo());
        StringBuilder sb = new StringBuilder();
        sb.append("Código: ").append(apto.getCodigo()).append(" | Nombre: ").append(apto.getNombre()).append("\n");
        sb.append("Bloque: ").append(apto.getBloque()).append(" | Piso: ").append(apto.getNumPiso()).append("\n");
        sb.append("Capacidad máxima: ").append(apto.getCapacidadMaxima()).append(" | Dormitorios: ").append(apto.getNumDormitorios()).append("\n");
        sb.append("Característica: ").append(apto.getCaracteristica()).append(" | Tarifa/persona: $").append(String.format("%.0f", apto.getTarifaPorPersona())).append("\n");
        sb.append("Estado operativo: ").append(apto.getEstadoOperativo()).append("\n");
        if (apto.getMotivo() != null) {
            sb.append("Motivo de bloqueo: ").append(apto.getMotivo()).append("\n");
        }
        sb.append("\n--- Reservas asociadas ---\n");
        for (Reserva r : mfc.getHotel().getHistorialGlobalReservas()) {
            if (r.getApartamentoAsignado().getCodigo().equals(apto.getCodigo())) {
                sb.append("• Titular: ").append(r.getTitular().getNombre()).append(" ").append(r.getTitular().getApellido())
                        .append(" (").append(r.getFechaEntrada()).append(" a ").append(r.getFechaSalida())
                        .append(") [").append(r.getEstado()).append("]\n");
            }
        }
        txtDetalleApto.setText(sb.toString());
    }

    @FXML
    public void onCambiarEstado() {
        try {
            String cod = cmbAptoEstado.getValue();
            EstadoOperativo nuevo = cmbNuevoEstado.getValue();
            if (cod == null || nuevo == null) throw new IllegalArgumentException("Selecciona un apartamento y un estado.");
            mfc.cambiarEstadoApartamento(cod, nuevo, txtMotivo.getText());
            refrescarTablero();
            Apartamento a = mfc.getHotel().buscarApartamento(cod);
            if (a != null) mostrarDetalle(a);
            if (nuevo == EstadoOperativo.PENDENTE_DE_PREPARACION || nuevo == EstadoOperativo.EN_PREPARACION) {
                new Alert(Alert.AlertType.INFORMATION, "Apartamento " + cod + " enviado a la Cola de Prioridad de Preparación.").showAndWait();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onActualizarTarifaApto() {
        try {
            String cod = cmbAptoEstado.getValue();
            if (cod == null) throw new IllegalArgumentException("Selecciona un apartamento.");
            double nueva = Double.parseDouble(txtNuevaTarifaApto.getText().trim());
            mfc.actualizarTarifaApartamento(cod, nueva);
            txtNuevaTarifaApto.clear();
            refrescarTablero();
            Apartamento a = mfc.getHotel().buscarApartamento(cod);
            if (a != null) mostrarDetalle(a);
            new Alert(Alert.AlertType.INFORMATION, "Tarifa actualizada a $" + String.format("%.0f", nueva)).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onCheckInRapido() {
        String cod = cmbAptoEstado.getValue();
        if (cod == null) return;
        for (Reserva r : mfc.getHotel().getHistorialGlobalReservas()) {
            if (r.getApartamentoAsignado().getCodigo().equals(cod) && r.getEstado().name().equals("CONFIRMADA")) {
                try {
                    mfc.checkIn(r);
                    refrescarTablero();
                    mostrarDetalle(r.getApartamentoAsignado());
                    return;
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                    return;
                }
            }
        }
        new Alert(Alert.AlertType.WARNING, "No hay reservas CONFIRMADAS pendientes de Check-In para este apartamento.").showAndWait();
    }

    @FXML
    public void onCheckOutRapido() {
        String cod = cmbAptoEstado.getValue();
        if (cod == null) return;
        for (Reserva r : mfc.getHotel().getHistorialGlobalReservas()) {
            if (r.getApartamentoAsignado().getCodigo().equals(cod) && r.getEstado().name().equals("EN_CURSO")) {
                try {
                    mfc.checkOut(r, false, LocalTime.now());
                    refrescarTablero();
                    mostrarDetalle(r.getApartamentoAsignado());
                    return;
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                    return;
                }
            }
        }
        new Alert(Alert.AlertType.WARNING, "No hay reservas EN_CURSO para Check-Out en este apartamento.").showAndWait();
    }
}
