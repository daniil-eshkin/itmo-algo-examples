package itmo.algo.examples.segtree;

import itmo.algo.examples.segtree.details.DelegateApplier;
import itmo.algo.examples.segtree.details.Node;
import itmo.algo.examples.segtree.details.ValueAggregator;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PersistentSegmentTree<V, D> extends AbstractSegmentTree<V, D> {
    public <U> PersistentSegmentTree(List<U> initialValues,
                           Function<U, V> converter,
                           ValueAggregator<V, Integer> aggregator,
                           DelegateApplier<D, V, Integer> applier,
                           BiFunction<D, D, D> composer,
                           D zeroDelegate) {
        this(
                initialValues.stream()
                        .map(converter)
                        .collect(Collectors.toList()),
                aggregator,
                applier,
                composer,
                zeroDelegate
        );
    }

    public PersistentSegmentTree(List<V> initialValues,
                       ValueAggregator<V, Integer> aggregator,
                       DelegateApplier<D, V, Integer> applier,
                       BiFunction<D, D, D> composer,
                       D zeroDelegate) {
        super(
                initialValues.size(),
                SegmentTree.build(
                        initialValues,
                        aggregator,
                        zeroDelegate,
                        0,
                        initialValues.size()
                ),
                aggregator,
                applier,
                composer,
                zeroDelegate
        );
    }

    public PersistentSegmentTree(int size,
                                 Node<V, D> root,
                                 ValueAggregator<V, Integer> aggregator,
                                 DelegateApplier<D, V, Integer> applier,
                                 BiFunction<D, D, D> composer,
                                 D zeroDelegate) {
        super(
                size,
                root,
                aggregator,
                applier,
                composer,
                zeroDelegate
        );
    }

    @Override
    public V get(int l, int r) {
        return get(root, l, r, zeroDelegate, 0, size);
    }

    @Override
    public void set(int l, int r, D d) {
        throw new UnsupportedOperationException();
    }

    public PersistentSegmentTree<V, D> setAndGet(int l, int r, D d) {
        return new PersistentSegmentTree<>(
                size,
                setAndGet(root, l, r, d, 0, size),
                aggregator,
                applier,
                composer,
                zeroDelegate
        );
    }

    private V get(Node<V, D> t, int l, int r, D delegate, int tl, int tr) {
        assert tl <= l && l < r && r <= tr;

        delegate = composer.apply(t.getDelegate(), delegate);
        if (tl == l && r == tr) {
            return applier.apply(delegate, t.getValue(), tl, tr);
        } else {
            int tm = (tl + tr) / 2;
            if (r <= tm) {
                return get(t.getL(), l, r, delegate, tl, tm);
            } else if (l >= tm) {
                return get(t.getR(), l, r, delegate, tm, tr);
            } else {
                return aggregator.apply(
                        get(t.getL(), l, tm, delegate, tl, tm),
                        get(t.getR(), tm, r, delegate, tm, tr),
                        tl, tr
                );
            }
        }
    }

    private Node<V, D> setAndGet(Node<V, D> t, int l, int r, D delegate, int tl, int tr) {
        assert tl <= l && l < r && r <= tr;

        var newDelegate = composer.apply(t.getDelegate(), delegate);
        if (tl == l && r == tr) {
            return new Node<>(
                    t.getValue(),
                    newDelegate,
                    t.getL(),
                    t.getR()
            );
        } else {
            int tm = (tl + tr) / 2;
            Node<V, D> left = t.getL();
            Node<V, D> right = t.getR();
            if (r <= tm) {
                left = setAndGet(t.getL(), l, r, newDelegate, tl, tm);
            } else if (l >= tm) {
                right = setAndGet(t.getR(), l, r, newDelegate, tm, tr);
            } else {
                left = setAndGet(t.getL(), l, tm, newDelegate, tl, tm);
                right = setAndGet(t.getR(), tm, r, newDelegate, tm, tr);
            }
            return new Node<>(
                    aggregator.apply(
                            getActualValue(left, tl, tm),
                            getActualValue(right, tm, tr),
                            tl, tr
                    ),
                    t.getDelegate(),
                    left,
                    right
            );
        }
    }

    private V getActualValue(Node<V, D> t, int tl, int tr) {
        return applier.apply(t.getDelegate(), t.getValue(), tl, tr);
    }

    @Override
    protected void pushDown(Node<V, D> t, int tl, int tr) {
        //Not used
    }
}
