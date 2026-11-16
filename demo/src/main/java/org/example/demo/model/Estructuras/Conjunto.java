package org.example.demo.model.Estructuras;

import java.util.Iterator;

public class Conjunto<T> implements Iterable<T> {
    private final ListaDoble<T> elementos = new ListaDoble<>();

    public boolean agregar(T dato) {
        if (dato == null || elementos.contiene(dato)) return false;
        elementos.agregarAlFinal(dato);
        return true;
    }

    public boolean contiene(T dato) { return elementos.contiene(dato); }
    public boolean eliminar(T dato) { return elementos.eliminar(dato); }
    public int getTam() { return elementos.getTam(); }

    @Override
    public Iterator<T> iterator() { return elementos.iterator(); }
}
