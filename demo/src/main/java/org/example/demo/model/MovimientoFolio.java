package org.example.pro_final_ed.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MovimientoFolio {
    public enum TipoMovimiento {
        CARGO,
        PAGO,
        REEMBOLSO_O_ANULACION
    }

    private final String descripcion;
    private final double monto;
    private final TipoMovimiento tipo;
    private final LocalDateTime fechaHora;

    public MovimientoFolio(String descripcion, double monto, boolean esCargo) {
        this(descripcion, monto, esCargo ? TipoMovimiento.CARGO : TipoMovimiento.PAGO);
    }

    public MovimientoFolio(String descripcion, double monto, TipoMovimiento tipo) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.tipo = tipo;
        this.fechaHora = LocalDateTime.now();
    }

    public String getDescripcion() { return descripcion; }
    public double getMonto() { return monto; }
    public TipoMovimiento getTipo() { return tipo; }
    public boolean isEsCargo() { return tipo == TipoMovimiento.CARGO; }
    public LocalDateTime getFechaHora() { return fechaHora; }

    @Override
    public String toString() {
        String etiqueta = switch (tipo) {
            case CARGO -> "[CARGO +]";
            case PAGO -> "[ABONO -]";
            case REEMBOLSO_O_ANULACION -> "[ANULACIÓN/REEMBOLSO]";
        };
        return fechaHora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                + " " + etiqueta + " " + descripcion + ": $" + String.format("%.0f", monto);
    }
}
