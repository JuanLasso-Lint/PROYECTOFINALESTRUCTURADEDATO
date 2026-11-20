package org.example.demo.model.Estructuras;

import org.example.demo.model.Exception.EstructuraVaciaException;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ListaCircularSimple<T> implements Iterable<T> {
    private Nodo<T> inicio;
    private Nodo<T> fin;
    private Nodo<T> turnoActual;
    private int tam;

    public void agregar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (inicio == null) {
            inicio = fin = turnoActual = nuevo;
            nuevo.setSiguiente(inicio);
        } else {
            fin.setSiguiente(nuevo);
            nuevo.setSiguiente(inicio);
            fin = nuevo;
        }
        tam++;
    }

    public T getActual() {
        if (turnoActual == null) throw new EstructuraVaciaException("La lista circular está vacía.");
        return turnoActual.getDato();
    }

    public T rotar() {
        if (turnoActual == null) throw new EstructuraVaciaException("La lista circular está vacía.");
        turnoActual = turnoActual.getSiguiente();
        return turnoActual.getDato();
    }

    public int getTam() { return tam; }
    public boolean estaVacia() { return tam == 0; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = inicio;
            private int visitados = 0;
            @Override public boolean hasNext() { return visitados < tam; } //Compara los nodos visitados contra el tamaño total de la lista (tam). Mientras queden nodos por leer, devuelve true
            @Override public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T d = actual.getDato();
                actual = actual.getSiguiente();
                visitados++;
                return d;
            }
        };
    }
}
