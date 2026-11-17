package org.example.demo.model;

import java.time.LocalDateTime;
import java.time.LocalTime;


public class TareaLimpieza {
    private final String codigoApartamento;
    private final boolean entradaHoy;
    private final LocalTime horaLlegadaSiguiente;
    private final LocalDateTime fechaSolicitud;

    public TareaLimpieza(String codigoApartamento, boolean entradaHoy) {
        this(codigoApartamento, entradaHoy, entradaHoy ? LocalTime.of(15, 0) : LocalTime.MAX);
    }

    public TareaLimpieza(String codigoApartamento, boolean entradaHoy, LocalTime horaLlegadaSiguiente) {
        this.codigoApartamento = codigoApartamento;
        this.entradaHoy = entradaHoy;
        this.horaLlegadaSiguiente = horaLlegadaSiguiente != null ? horaLlegadaSiguiente : LocalTime.MAX;
        this.fechaSolicitud = LocalDateTime.now();
    }

    public String getCodigoApartamento() { return codigoApartamento; }
    public boolean isEntradaHoy() { return entradaHoy; }
    public LocalTime getHoraLlegadaSiguiente() { return horaLlegadaSiguiente; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TareaLimpieza other)) return false;
        return this.codigoApartamento.equals(other.codigoApartamento);
    }

    @Override
    public int hashCode() { return codigoApartamento.hashCode(); }

    @Override
    public String toString() {
        return "Apto " + codigoApartamento + (entradaHoy
                ? " [PRIORIDAD 1 - Entrada hoy " + horaLlegadaSiguiente + "]"
                : " [PRIORIDAD 2 - Sin entrada hoy]");
    }
}
