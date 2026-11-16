package org.example.demo.model.Estructuras;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ListaDoble<T> implements Iterable<T> {
    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tam;

    public void agregarAlFinal(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (cabeza == null) {
            cabeza = cola = nuevo;
        } else {
            cola.setSiguiente(nuevo);
            nuevo.setAnterior(cola);
            cola = nuevo;
        }
        tam++;
    }

    public void agregarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (cabeza == null) {
            cabeza = cola = nuevo;
        } else {
            nuevo.setSiguiente(cabeza);
            cabeza.setAnterior(nuevo);
            cabeza = nuevo;
        }
        tam++;
    }

    public boolean contiene(T dato) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (actual.getDato().equals(dato)) return true;
            actual = actual.getSiguiente();
        }
        return false;
    }

    public boolean eliminar(T dato) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (actual.getDato().equals(dato)) {
                if (actual.getAnterior() != null) actual.getAnterior().setSiguiente(actual.getSiguiente());
                else cabeza = actual.getSiguiente();

                if (actual.getSiguiente() != null) actual.getSiguiente().setAnterior(actual.getAnterior());
                else cola = actual.getAnterior();

                tam--;
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }

    public T obtener(int index) {
        if (index < 0 || index >= tam) throw new IndexOutOfBoundsException("Índice inválido: " + index);
        Nodo<T> actual = cabeza;
        for (int i = 0; i < index; i++) actual = actual.getSiguiente();
        return actual.getDato();
    }

    public void ordenar(Comparator<T> comparador) {
        if (tam < 2 || comparador == null) return;
        boolean cambiado;
        do {
            cambiado = false;
            Nodo<T> actual = cabeza;
            while (actual != null && actual.getSiguiente() != null) {
                if (comparador.compare(actual.getDato(), actual.getSiguiente().getDato()) > 0) {
                    T temp = actual.getDato();
                    actual.setDato(actual.getSiguiente().getDato());
                    actual.getSiguiente().setDato(temp);
                    cambiado = true;
                }
                actual = actual.getSiguiente();
            }
        } while (cambiado);
    }

    public int getTam() { return tam; }
    public boolean estaVacia() { return tam == 0; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Nodo<T> actual = cabeza;
        while (actual != null) {
            sb.append(actual.getDato());
            if (actual.getSiguiente() != null) sb.append(" -> ");
            actual = actual.getSiguiente();
        }
        return sb.append("]").toString();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cabeza;
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
