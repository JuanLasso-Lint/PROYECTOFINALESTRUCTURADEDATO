package org.example.demo.model.Estructuras;


import org.example.demo.model.Exception.EstructuraVaciaException;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class ArbolBinarioBusqueda<T extends Comparable<T>> implements Iterable<T> {

    private NodoArbol<T> raiz;
    private int tam;

    public ArbolBinarioBusqueda() {
        this.raiz = null;
        this.tam = 0;
    }




    //verifica que el dato no sea null y que no exista ya (buscar(dato)).
    // Si está repetido, devuelve false
    public boolean insertar(T dato) {
        if (dato == null || buscar(dato)) {
            return false;
        }
        raiz = insertarRec(raiz, dato);
        tam++;
        return true;
    }






    //baja recursivamente comparando dato.compareTo(actual.getDato()):
    // si da negativo va a la izquierda; si da positivo va a la derecha.

    private NodoArbol<T> insertarRec(NodoArbol<T> actual, T dato) {
        if (actual == null) return new NodoArbol<>(dato);
        int comparacion = dato.compareTo(actual.getDato());
        if (comparacion < 0) {
            actual.setIzquierdo(insertarRec(actual.getIzquierdo(), dato));
        } else if (comparacion > 0) {
            actual.setDerecho(insertarRec(actual.getDerecho(), dato));
        }
        return actual;
    }





//"¿lo que busco es menor o mayor que tú?"
// y así ignoras la mitad de la casa en cada paso hasta dar directo

    public boolean buscar(T datoBuscar) {
        return buscarElemento(datoBuscar) != null;
    }

    public T buscarElemento(T datoBuscar) {
        if (datoBuscar == null) return null;
        return buscarElementoRec(raiz, datoBuscar);
    }

    private T buscarElementoRec(NodoArbol<T> actual, T datoBuscar) {
        if (actual == null) return null;
        int comparacion = datoBuscar.compareTo(actual.getDato());
        if (comparacion == 0) return actual.getDato();
        return comparacion < 0
                ? buscarElementoRec(actual.getIzquierdo(), datoBuscar)
                : buscarElementoRec(actual.getDerecho(), datoBuscar);
    }








//evisa todos los cuartos —izquierda y derecha
// — hasta que alguno diga "¡yo cumplo la pista!".
    public boolean buscarRecursivo(Predicate<T> condicion) {
        return buscarRecursivoAux(raiz, condicion);
    }

    private boolean buscarRecursivoAux(NodoArbol<T> actual, Predicate<T> condicion) {
        if (actual == null) return false;
        if (condicion.test(actual.getDato())) return true;
        return buscarRecursivoAux(actual.getIzquierdo(), condicion)
                || buscarRecursivoAux(actual.getDerecho(), condicion);
    }

















    public void eliminar(T datoEliminar) {
        if (raiz == null) {
            throw new EstructuraVaciaException("El árbol está vacío, no se puede eliminar.");
        }
        if (buscar(datoEliminar)) {
            raiz = eliminarRec(raiz, datoEliminar);
            tam--;
        }
    }


    // si lolo tieene debajp un nodo engancha al de arriba con el de abajp
    // si tiene dos nodo abajo

    private NodoArbol<T> eliminarRec(NodoArbol<T> actual, T datoEliminar) {
        if (actual == null) return null;
        int comparacion = datoEliminar.compareTo(actual.getDato());
        if (comparacion < 0) {
            actual.setIzquierdo(eliminarRec(actual.getIzquierdo(), datoEliminar));
        } else if (comparacion > 0) {
            actual.setDerecho(eliminarRec(actual.getDerecho(), datoEliminar));
        } else {
            if (actual.getIzquierdo() == null) return actual.getDerecho();
            if (actual.getDerecho() == null) return actual.getIzquierdo();
            T sucesor = encontrarMinimo(actual.getDerecho());
            actual.setDato(sucesor);
            actual.setDerecho(eliminarRec(actual.getDerecho(), sucesor));
        }
        return actual;
    }




// baja un paso a la derecha y luego se desliza todo a la izquierda
    private T encontrarMinimo(NodoArbol<T> nodo) {
        while (nodo.getIzquierdo() != null) nodo = nodo.getIzquierdo();
        return nodo.getDato();
    }




//Cuenta cuántos pisos de cuartos
// hay desde el techo hasta el cuarto que quedó más abajo en el suelo.
    public int altura() { return alturaRecursiva(raiz); }

    private int alturaRecursiva(NodoArbol<T> nodo) {
        if (nodo == null) return 0;
        return 1 + Math.max(alturaRecursiva(nodo.getIzquierdo()), alturaRecursiva(nodo.getDerecho()));
    }









    public int getTam() { return tam; }
    public boolean estaVacio() { return raiz == null; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Pila<NodoArbol<T>> pila = new Pila<>();
            { apilarIzquierdos(raiz); }

            private void apilarIzquierdos(NodoArbol<T> nodo) {
                while (nodo != null) {
                    pila.apilar(nodo);
                    nodo = nodo.getIzquierdo();
                }
            }



            //verifica si aún quedan nodos pendientes en la pila, y next() saca el nodo de la cima,
            // deja preparada su rama derecha (si tiene) y devuelve su dato.
            @Override public boolean hasNext() { return !pila.estaVacia(); }
            @Override public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                NodoArbol<T> actual = pila.desapilar();
                if (actual.getDerecho() != null) apilarIzquierdos(actual.getDerecho());
                return actual.getDato();
            }
        };
    }
}

