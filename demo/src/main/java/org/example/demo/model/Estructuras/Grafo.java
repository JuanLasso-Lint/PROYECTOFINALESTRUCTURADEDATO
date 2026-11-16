package org.example.demo.model.Estructuras;

import java.util.Comparator;
import java.util.Iterator;

public class Grafo<T extends Comparable<T>> implements Iterable<T> {

    public static class Arista<T> {
        private final T destino;
        private double peso;

        public Arista(T destino, double peso) {
            this.destino = destino;
            this.peso = peso;
        }

        public T getDestino() { return destino; }
        public double getPeso() { return peso; }
        public void setPeso(double peso) { this.peso = peso; }
    }

    private static class EstadoDijkstra<T> {
        final T nodo;
        final double distancia;
        EstadoDijkstra(T nodo, double distancia) {
            this.nodo = nodo;
            this.distancia = distancia;
        }
    }

    private final ArbolBinarioBusqueda<ParClaveValor<T, ListaDoble<Arista<T>>>> adyacencias = new ArbolBinarioBusqueda<>();

    private ListaDoble<Arista<T>> obtenerListaAristas(T v) {
        if (v == null) return null;
        ParClaveValor<T, ListaDoble<Arista<T>>> par = adyacencias.buscarElemento(new ParClaveValor<>(v, null));
        return par != null ? par.getValor() : null;
    }

    public void agregarVertice(T v) {
        if (v != null && !contieneVertice(v)) {
            adyacencias.insertar(new ParClaveValor<>(v, new ListaDoble<>()));
        }
    }

    public boolean contieneVertice(T v) {
        return obtenerListaAristas(v) != null;
    }

    public void agregarArista(T origen, T destino, double peso) {
        if (origen == null || destino == null || origen.equals(destino)) return;
        agregarVertice(origen);
        agregarVertice(destino);
        actualizarOAgregar(origen, destino, peso);
        actualizarOAgregar(destino, origen, peso);
    }

    private void actualizarOAgregar(T u, T v, double peso) {
        ListaDoble<Arista<T>> lista = obtenerListaAristas(u);
        for (Arista<T> a : lista) {
            if (a.getDestino().equals(v)) {
                a.setPeso(peso);
                return;
            }
        }
        lista.agregarAlFinal(new Arista<>(v, peso));
    }

    public Double obtenerPesoArista(T u, T v) {
        ListaDoble<Arista<T>> lista = obtenerListaAristas(u);
        if (lista == null) return null;
        for (Arista<T> a : lista) {
            if (a.getDestino().equals(v)) return a.getPeso();
        }
        return null;
    }

    public boolean eliminarArista(T u, T v) {
        boolean e1 = eliminarDirigida(u, v);
        boolean e2 = eliminarDirigida(v, u);
        return e1 || e2;
    }

    private boolean eliminarDirigida(T u, T v) {
        ListaDoble<Arista<T>> lista = obtenerListaAristas(u);
        if (lista == null) return false;
        Arista<T> target = null;
        for (Arista<T> a : lista) {
            if (a.getDestino().equals(v)) {
                target = a;
                break;
            }
        }
        return target != null && lista.eliminar(target);
    }

    public ListaDoble<T> obtenerAdyacentes(T v) {
        ListaDoble<T> res = new ListaDoble<>();
        ListaDoble<Arista<T>> lista = obtenerListaAristas(v);
        if (lista != null) {
            for (Arista<T> a : lista) {
                res.agregarAlFinal(a.getDestino());
            }
        }
        return res;
    }

    public ListaDoble<T> dijkstraRutaMasCorta(T origen, T destino) {
        if (!contieneVertice(origen) || !contieneVertice(destino)) {
            throw new IllegalArgumentException("El vértice origen o destino no existe en el mapa del edificio.");
        }

        ArbolBinarioBusqueda<ParClaveValor<T, Double>> dist = new ArbolBinarioBusqueda<>();
        ArbolBinarioBusqueda<ParClaveValor<T, T>> previo = new ArbolBinarioBusqueda<>();
        Conjunto<T> visitados = new Conjunto<>();

        for (T v : this) {
            dist.insertar(new ParClaveValor<>(v, Double.POSITIVE_INFINITY));
        }
        dist.buscarElemento(new ParClaveValor<>(origen, null)).setValor(0.0);

        ColaPrioridad<EstadoDijkstra<T>> pq = new ColaPrioridad<>(Comparator.comparingDouble(e -> e.distancia));
        pq.encolar(new EstadoDijkstra<>(origen, 0.0));

        while (!pq.estaVacia()) {
            EstadoDijkstra<T> actualEst = pq.desencolar();
            T actual = actualEst.nodo;
            if (!visitados.agregar(actual)) continue;
            if (actual.equals(destino)) break;

            double distActual = dist.buscarElemento(new ParClaveValor<>(actual, null)).getValor();
            for (Arista<T> arista : obtenerListaAristas(actual)) {
                T vecino = arista.getDestino();
                if (visitados.contiene(vecino)) continue;
                double nuevaDist = distActual + arista.getPeso();
                ParClaveValor<T, Double> parVecino = dist.buscarElemento(new ParClaveValor<>(vecino, null));
                if (nuevaDist < parVecino.getValor()) {
                    parVecino.setValor(nuevaDist);
                    ParClaveValor<T, T> parPrev = previo.buscarElemento(new ParClaveValor<>(vecino, null));
                    if (parPrev == null) {
                        previo.insertar(new ParClaveValor<>(vecino, actual));
                    } else {
                        parPrev.setValor(actual);
                    }
                    pq.encolar(new EstadoDijkstra<>(vecino, nuevaDist));
                }
            }
        }

        ListaDoble<T> ruta = new ListaDoble<>();
        if (dist.buscarElemento(new ParClaveValor<>(destino, null)).getValor() == Double.POSITIVE_INFINITY) {
            return ruta;
        }
        T paso = destino;
        while (paso != null) {
            ruta.agregarAlInicio(paso);
            ParClaveValor<T, T> p = previo.buscarElemento(new ParClaveValor<>(paso, null));
            paso = (p != null) ? p.getValor() : null;
        }
        return ruta;
    }

    public Conjunto<T> bfsAlcanzables(T origen) {
        if (!contieneVertice(origen)) {
            throw new IllegalArgumentException("El nodo de origen no existe en el grafo: " + origen);
        }
        Conjunto<T> visitados = new Conjunto<>();
        Cola<T> cola = new Cola<>();
        visitados.agregar(origen);
        cola.encolar(origen);

        while (!cola.estaVacia()) {
            T actual = cola.desencolar();
            for (Arista<T> a : obtenerListaAristas(actual)) {
                if (visitados.agregar(a.getDestino())) {
                    cola.encolar(a.getDestino());
                }
            }
        }
        return visitados;
    }

    public ListaDoble<T> recorridoDFS(T origen) {
        if (!contieneVertice(origen)) {
            throw new IllegalArgumentException("El nodo de origen no existe en el grafo: " + origen);
        }
        Conjunto<T> visitados = new Conjunto<>();
        ListaDoble<T> orden = new ListaDoble<>();
        dfsRecursivo(origen, visitados, orden);
        return orden;
    }

    private void dfsRecursivo(T actual, Conjunto<T> visitados, ListaDoble<T> orden) {
        visitados.agregar(actual);
        orden.agregarAlFinal(actual);
        for (Arista<T> a : obtenerListaAristas(actual)) {
            if (!visitados.contiene(a.getDestino())) {
                dfsRecursivo(a.getDestino(), visitados, orden);
            }
        }
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<ParClaveValor<T, ListaDoble<Arista<T>>>> it = adyacencias.iterator();
        return new Iterator<T>() {
            @Override public boolean hasNext() { return it.hasNext(); }
            @Override public T next() { return it.next().getClave(); }
        };
    }
}
