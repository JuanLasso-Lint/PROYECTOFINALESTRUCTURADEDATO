package org.example.demo.model.Estructuras;

import org.example.demo.model.Exception.EstructuraVaciaException;

import java.util.Iterator;
import java.util.NoSuchElementException;


public class Pila<T> implements Iterable<T> {
    private Nodo<T> cima;
    private int tam;

    public void apilar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.setSiguiente(cima);
        cima = nuevo;
        tam++;
    }

    public T desapilar() {
        if (estaVacia()) throw new EstructuraVaciaException("La pila está vacía.");
        T dato = cima.getDato();
        cima = cima.getSiguiente();
        tam--;
        return dato;
    }

    public T verCima() {
        if (estaVacia()) throw new EstructuraVaciaException("La pila está vacía.");
        return cima.getDato();
    }

    public boolean estaVacia() { return cima == null; }
    public int getTam() { return tam; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cima;
            @Override public boolean hasNext() { return actual != null; }
            @Override public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T d = actual.getDato();
                actual = actual.getSiguiente();
                return d;
            }
        };
    }
}
