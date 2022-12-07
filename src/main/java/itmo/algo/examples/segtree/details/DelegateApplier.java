package itmo.algo.examples.segtree.details;

public interface DelegateApplier<D, V, I> {
    V apply(D d, V v, I tl, I tr);
}
