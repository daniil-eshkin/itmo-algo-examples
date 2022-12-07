package itmo.algo.examples.segtree.details;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Node<V, D> {
    private V value;
    private D delegate;
    private Node<V, D> l;
    private Node<V, D> r;

    public Node(V value, D delegate) {
        this.value = value;
        this.delegate = delegate;
        this.l = null;
        this.r = null;
    }

    public Node(Node<V, D> l, Node<V, D> r,
                ValueAggregator<V, Integer> aggregator,
                int tl, int tr,
                D delegate) {
        this.value = aggregator.apply(l.value, r.value, tl, tr);
        this.delegate = delegate;
        this.l = l;
        this.r = r;
    }
}
