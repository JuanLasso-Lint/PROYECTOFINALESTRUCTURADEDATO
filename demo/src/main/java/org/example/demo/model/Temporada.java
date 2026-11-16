package org.example.demo.model;

import java.time.LocalDate;

public class Temporada implements Comparable<Temporada> {
    private final String nombre;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final double multiplicador;

    public Temporada(String nombre, LocalDate fechaInicio, LocalDate fechaFin, double multiplicador) {
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.multiplicador = multiplicador;
    }

    public boolean aplicaA(LocalDate fecha) {
        return !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }

    public String getNombre() { return nombre; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public double getMultiplicador() { return multiplicador; }

    @Override
    public int compareTo(Temporada o) {
        return this.fechaInicio.compareTo(o.fechaInicio);
    }

    @Override
    public String toString() {
        return nombre + " (" + fechaInicio + " a " + fechaFin + ", x" + multiplicador + ")";
    }
}
