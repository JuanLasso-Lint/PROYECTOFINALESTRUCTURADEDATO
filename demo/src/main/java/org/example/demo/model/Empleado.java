package org.example.demo.model;

public class Empleado implements Comparable<Empleado> {
    private final String nombre;
    private final String codigo;
    private final String cargo;

    public Empleado(String nombre, String codigo, String cargo) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.cargo = cargo;
    }

    public String getNombre() { return nombre; }
    public String getCodigo() { return codigo; }
    public String getCargo() { return cargo; }

    @Override
    public int compareTo(Empleado o) {
        return this.codigo.compareTo(o.codigo);
    }
}
