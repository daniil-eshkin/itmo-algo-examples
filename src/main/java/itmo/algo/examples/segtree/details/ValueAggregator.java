package itmo.algo.examples.segtree.details;

public interface ValueAggregator<V, I> {
    V apply(V a, V b, I tl, I tm, I tr);
}
