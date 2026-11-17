package org.example.demo.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.demo.controller.ModelFactoryController;
import org.example.demo.model.Apartamento;
import org.example.demo.model.Huesped;
import org.example.demo.model.MovimientoFolio;
import org.example.demo.model.Reserva;


import java.time.LocalDate;

public class UsuarioViewController {

    @FXML private TextField txtRegNombreH;
    @FXML private TextField txtRegApellidoH;
    @FXML private TextField txtRegDocH;
    @FXML private DatePicker dpRegFechaNac;
    @FXML private TextField txtRegTelH;
    @FXML private TextField txtRegCorreoH;
    @FXML private TextField txtRegFotoH;

    @FXML private TextField txtDocConsulta;
    @FXML private TextField txtAptoFavorito;
    @FXML private TextArea txtPerfilHuesped;

    private final ModelFactoryController mfc = ModelFactoryController.getInstance();

    @FXML
    public void initialize() {
        if (!txtDocConsulta.getText().trim().isEmpty()) onConsultarHuesped();
    }

    @FXML
    public void onRegistrarHuesped() {
        try {
            String nom = txtRegNombreH.getText().trim();
            String ape = txtRegApellidoH.getText().trim();
            String doc = txtRegDocH.getText().trim();
            LocalDate fechaNac = dpRegFechaNac.getValue();
            int tel = Integer.parseInt(txtRegTelH.getText().trim());
            String correo = txtRegCorreoH.getText().trim();
            String foto = txtRegFotoH.getText().trim().isEmpty() ? null : txtRegFotoH.getText().trim();

            if (nom.isEmpty() || doc.isEmpty() || fechaNac == null) {
                throw new IllegalArgumentException("Nombre, documento y fecha de nacimiento son obligatorios.");
            }

            mfc.registrarHuesped(nom, ape, doc, fechaNac, tel, correo, foto);
            txtDocConsulta.setText(doc);
            txtRegNombreH.clear();
            txtRegApellidoH.clear();
            txtRegDocH.clear();
            txtRegTelH.clear();
            txtRegCorreoH.clear();
            txtRegFotoH.clear();
            onConsultarHuesped();
            new Alert(Alert.AlertType.INFORMATION, "Huésped " + nom + " registrado correctamente.").showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error al registrar huésped: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onConsultarHuesped() {
        String doc = txtDocConsulta.getText().trim();
        Huesped h = mfc.getHotel().buscarHuesped(doc);
        if (h == null) {
            txtPerfilHuesped.setText("No se encontró ningún huésped registrado con documento: " + doc);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== PERFIL DEL HUÉSPED ===\n");
        sb.append("Nombre: ").append(h.getNombre()).append(" ").append(h.getApellido()).append("\n");
        sb.append("Documento: ").append(h.getDocumento()).append(" | Edad: ").append(h.getEdad()).append(" años | Tel: ").append(h.getNumeroTelefono()).append("\n");
        sb.append("Fecha Nacimiento: ").append(h.getFecha()).append(" | Correo: ").append(h.getCorreo()).append("\n");
        sb.append("Fotografía (opcional): ").append(h.getRutaFotografia() != null ? h.getRutaFotografia() : "Sin fotografía registrada").append("\n");
        sb.append("Apartamentos Favoritos (Conjunto sin duplicados): ");
        for (Apartamento fav : h.getFavoritos()) sb.append(fav.getCodigo()).append(" ");
        sb.append("\n\n=== RESERVAS Y ESTADO DE CUENTA (FOLIO) ===\n");
        for (Reserva r : mfc.getHotel().getHistorialGlobalReservas()) {
            if (r.getTitular().getDocumento().equals(doc)) {
                sb.append("#").append(r.getId()).append(" | Apto ").append(r.getApartamentoAsignado().getCodigo())
                        .append(" | Fechas: ").append(r.getFechaEntrada()).append(" a ").append(r.getFechaSalida())
                        .append(" | Estado: ").append(r.getEstado())
                        .append(" | Cargos: $").append(String.format("%.0f", r.calcularTotalCargos()))
                        .append(" | Pagado Neto: $").append(String.format("%.0f", r.calcularTotalPagadoNeto())).append("\n");
                for (MovimientoFolio mf : r.getFolio()) {
                    sb.append("   -> ").append(mf.toString()).append("\n");
                }
            }
        }
        txtPerfilHuesped.setText(sb.toString());
    }

    @FXML
    public void onAgregarFavorito() {
        String doc = txtDocConsulta.getText().trim();
        String codApto = txtAptoFavorito.getText().trim();
        Huesped h = mfc.getHotel().buscarHuesped(doc);
        Apartamento apto = mfc.getHotel().buscarApartamento(codApto);
        if (h == null || apto == null) {
            new Alert(Alert.AlertType.WARNING, "Verifica que tanto el documento del huésped como el código de apartamento existan.").showAndWait();
            return;
        }
        h.getFavoritos().agregar(apto);
        onConsultarHuesped();
    }

    private Reserva obtenerUltimaReservaActiva(String doc) {
        Reserva ultima = null;
        for (Reserva r : mfc.getHotel().getHistorialGlobalReservas()) {
            if (r.getTitular().getDocumento().equals(doc) &&
                    r.getEstado() != org.example.demo.model.EstadoReserva.CANCELADA &&
                    r.getEstado() != org.example.demo.model.EstadoReserva.FINALIZADA) {
                ultima = r;
            }
        }
        return ultima;
    }

    @FXML
    public void onPagarAnticipo() {
        try {
            String doc = txtDocConsulta.getText().trim();
            Reserva r = obtenerUltimaReservaActiva(doc);
            if (r == null) throw new IllegalArgumentException("El huésped no tiene reservas activas/pendientes.");
            double total = r.calcularTotalCargos();
            double anticipo = total * 0.30;
            mfc.pagarAnticipoPortal(r, anticipo, total);
            onConsultarHuesped();
            new Alert(Alert.AlertType.INFORMATION, "Anticipo del 30% ($" + String.format("%.0f", anticipo) + ") pagado. Reserva CONFIRMADA.").showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onDeshacerUltimoMovimientoFolio() {
        try {
            String doc = txtDocConsulta.getText().trim();
            Reserva r = obtenerUltimaReservaActiva(doc);
            if (r == null) throw new IllegalArgumentException("El huésped no tiene reservas activas.");
            MovimientoFolio deshecho = r.deshacerUltimoMovimiento();
            onConsultarHuesped();
            new Alert(Alert.AlertType.INFORMATION, "Se registró el movimiento contrario para: " + deshecho.getDescripcion()).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onCancelarReservaHuesped() {
        try {
            String doc = txtDocConsulta.getText().trim();
            Reserva r = obtenerUltimaReservaActiva(doc);
            if (r == null) throw new IllegalArgumentException("El huésped no tiene reservas activas para cancelar.");
            double reembolso = mfc.cancelarReserva(r, LocalDate.now());
            onConsultarHuesped();
            new Alert(Alert.AlertType.INFORMATION, "Reserva #" + r.getId() + " cancelada. Reembolso calculado y aplicado: $" + String.format("%.0f", reembolso)).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }
}
