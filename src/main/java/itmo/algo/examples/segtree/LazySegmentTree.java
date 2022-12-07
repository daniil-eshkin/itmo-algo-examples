package itmo.algo.examples.segtree;

import itmo.algo.examples.segtree.details.DelegateApplier;
import itmo.algo.examples.segtree.details.Node;
import itmo.algo.examples.segtree.details.ValueAggregator;

import java.util.function.BiFunction;

public class LazySegmentTree<V, D> extends AbstractSegmentTree<V, D> {
    private final BiFunction<Integer, Integer, V> init;

    public LazySegmentTree(int size,
                           ValueAggregator<V, Integer> aggregator,
                           DelegateApplier<D, V, Integer> applier,
                           BiFunction<D, D, D> composer,
                           BiFunction<Integer, Integer, V> init,
                           D zeroDelegate) {
        super(
                size,
                new Node<>(init.apply(0, size), zeroDelegate),
                aggregator,
                applier,
                composer,
                zeroDelegate
        );
        this.init = init;
    }

    @Override
    protected void pushDown(Node<V, D> t, int tl, int tr) {
        t.setValue(applier.apply(t.getDelegate(), t.getValue(), tl, tr));
        if (tl + 1 < tr) {
            if (t.getL() == null) {
                assert t.getR() == null;

                int tm = (tl + tr) / 2;
                t.setL(new Node<>(init.apply(tl, tm), zeroDelegate));
                t.setR(new Node<>(init.apply(tm, tr), zeroDelegate));
            }
            t.getL().setDelegate(composer.apply(t.getL().getDelegate(), t.getDelegate()));
            t.getR().setDelegate(composer.apply(t.getR().getDelegate(), t.getDelegate()));
        }
        t.setDelegate(zeroDelegate);
    }
}
