package org.example.demo.model;

public class Apartamento implements Comparable<Apartamento> {
    private final String codigo;
    private final String nombre;
    private final int bloque;
    private final int numPiso;
    private final int numDormitorios;
    private final int capacidadMaxima;
    private final String caracteristica;
    private double tarifaPorPersona;
    private EstadoOperativo estadoOperativo;
    private String motivo;

    public Apartamento(String codigo, String nombre, int bloque, int numPiso,
                       int numDormitorios, int capacidadMaxima, String caracteristica,
                       double tarifaPorPersona, EstadoOperativo estadoOperativo, String motivo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.bloque = bloque;
        this.numPiso = numPiso;
        this.numDormitorios = numDormitorios;
        this.capacidadMaxima = capacidadMaxima;
        this.caracteristica = caracteristica;
        this.tarifaPorPersona = tarifaPorPersona;
        this.estadoOperativo = estadoOperativo != null ? estadoOperativo : EstadoOperativo.PREPARADO;
        this.motivo = motivo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getBloque() {
        return bloque;
    }

    public int getNumPiso() {
        return numPiso;
    }

    public int getNumDormitorios() {
        return numDormitorios;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public String getCaracteristica() {
        return caracteristica;
    }

    public double getTarifaPorPersona() {
        return tarifaPorPersona;
    }

    public void setTarifaPorPersona(double tarifaPorPersona) {
        this.tarifaPorPersona = tarifaPorPersona;
    }

    public EstadoOperativo getEstadoOperativo() {
        return estadoOperativo;
    }

    public void setEstadoOperativo(EstadoOperativo estadoOperativo) {
        this.estadoOperativo = estadoOperativo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    @Override
    public int compareTo(Apartamento o) {
        return this.codigo.compareTo(o.codigo);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Apartamento other)) return false;
        return this.codigo.equals(other.codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (Bloque " + bloque + ", Dorm: " + numDormitorios + ", Cap: " + capacidadMaxima + ", $" + String.format("%.0f", tarifaPorPersona) + ")";
    }
}
