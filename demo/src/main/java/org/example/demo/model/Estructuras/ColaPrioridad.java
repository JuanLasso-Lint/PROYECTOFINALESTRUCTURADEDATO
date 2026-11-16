package org.example.demo.model.Estructuras;

import org.example.demo.model.Exception.EstructuraVaciaException;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ColaPrioridad<T> implements Iterable<T> {
    private Nodo<T> frente;
    private int tam;
    private final Comparator<T> comparador;

    public ColaPrioridad(Comparator<T> comparador) {
        if (comparador == null) {
            throw new IllegalArgumentException("El comparador no puede ser nulo.");
        }
        this.comparador = comparador;
    }

    public void encolar(T dato) {
        if (dato == null) return;
        Nodo<T> nuevo = new Nodo<>(dato);
        if (frente == null || comparador.compare(dato, frente.getDato()) < 0) {
            nuevo.setSiguiente(frente);
            frente = nuevo;
        } else {
            Nodo<T> actual = frente;
            while (actual.getSiguiente() != null &&
                   comparador.compare(dato, actual.getSiguiente().getDato()) >= 0) {
                actual = actual.getSiguiente();
            }
            nuevo.setSiguiente(actual.getSiguiente());
            actual.setSiguiente(nuevo);
        }
        tam++;
    }

    public T desencolar() {
        if (estaVacia()) throw new EstructuraVaciaException("La cola de prioridad está vacía.");
        T dato = frente.getDato();
        frente = frente.getSiguiente();
        tam--;
        return dato;
    }

    public T verFrente() {
        if (estaVacia()) throw new EstructuraVaciaException("La cola de prioridad está vacía.");
        return frente.getDato();
    }

    public boolean contiene(T dato) {
        Nodo<T> actual = frente;
        while (actual != null) {
            if (actual.getDato().equals(dato)) return true;
            actual = actual.getSiguiente();
        }
        return false;
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
