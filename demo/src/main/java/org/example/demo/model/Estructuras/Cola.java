package org.example.demo.model.Estructuras;

import org.example.demo.model.Exception.EstructuraVaciaException;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Cola<T> implements Iterable<T> {
    private Nodo<T> frente;
    private Nodo<T> fin;
    private int tam;

    public void encolar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            frente = fin = nuevo;
        } else {
            fin.setSiguiente(nuevo);
            fin = nuevo;
        }
        tam++;
    }

    public T desencolar() {
        if (estaVacia()) throw new EstructuraVaciaException("La cola está vacía.");
        T dato = frente.getDato();
        frente = frente.getSiguiente();
        if (frente == null) fin = null;
        tam--;
        return dato;
    }

    public T verFrente() {
        if (estaVacia()) throw new EstructuraVaciaException("La cola está vacía.");
        return frente.getDato();
    }

    public boolean estaVacia() { return frente == null; }


    public int getTam() { return tam; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = frente;
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
