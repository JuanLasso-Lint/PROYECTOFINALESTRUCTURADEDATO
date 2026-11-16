package org.example.demo.model.Estructuras;

public class ParClaveValor<K extends Comparable<K>, V> implements Comparable<ParClaveValor<K, V>> {
    private final K clave;
    private V valor;

    public ParClaveValor(K clave, V valor) {
        this.clave = clave;
        this.valor = valor;
    }

    public K getClave() { return clave; }
    public V getValor() { return valor; }
    public void setValor(V valor) { this.valor = valor; }

    @Override
    public int compareTo(ParClaveValor<K, V> o) {
        return this.clave.compareTo(o.clave);
    }
}
