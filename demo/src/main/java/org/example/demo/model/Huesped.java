package org.example.demo.model;

import org.example.demo.model.Estructuras.Conjunto;

import java.time.LocalDate;
import java.time.Period;

public class Huesped implements Comparable<Huesped> {
    private final String nombre;
    private final String apellido;
    private final String documento;
    private final LocalDate fecha;
    private final int numeroTelefono;
    private final String correo;
    private String rutaFotografia;
    private final Conjunto<Apartamento> favoritos;

    public Huesped(String nombre, String apellido, String documento, LocalDate fecha, int numeroTelefono, String correo) {
        this(nombre, apellido, documento, fecha, numeroTelefono, correo, null);
    }

    public Huesped(String nombre, String apellido, String documento, LocalDate fecha, int numeroTelefono, String correo, String rutaFotografia) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.documento = documento;
        this.fecha = fecha;
        this.numeroTelefono = numeroTelefono;
        this.correo = correo;
        this.rutaFotografia = rutaFotografia;
        this.favoritos = new Conjunto<>();
    }

    public int getEdad() {
        return fecha != null ? Period.between(fecha, LocalDate.now()).getYears() : 30;
    }




    public boolean esMenorDeSeisAnios() {
        return getEdad() < 6;
    }




    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDocumento() { return documento; }
    public LocalDate getFecha() { return fecha; }
    public int getNumeroTelefono() { return numeroTelefono; }
    public String getCorreo() { return correo; }
    public String getRutaFotografia() { return rutaFotografia; }
    public void setRutaFotografia(String rutaFotografia) { this.rutaFotografia = rutaFotografia; }
    public Conjunto<Apartamento> getFavoritos() { return favoritos; }

    @Override
    public int compareTo(Huesped o) {
        return this.documento.compareTo(o.documento);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Huesped other)) return false;
        return this.documento.equals(other.documento);
    }

    @Override
    public int hashCode() { return documento.hashCode(); }
}
