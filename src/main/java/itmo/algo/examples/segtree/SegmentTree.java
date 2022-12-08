package itmo.algo.examples.segtree;

import itmo.algo.examples.segtree.details.DelegateApplier;
import itmo.algo.examples.segtree.details.Node;
import itmo.algo.examples.segtree.details.ValueAggregator;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SegmentTree<V, D> extends AbstractSegmentTree<V, D> {
    public <U> SegmentTree(List<U> initialValues,
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

    public SegmentTree(List<V> initialValues,
                       ValueAggregator<V, Integer> aggregator,
                       DelegateApplier<D, V, Integer> applier,
                       BiFunction<D, D, D> composer,
                       D zeroDelegate) {
        super(
                initialValues.size(),
                build(
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

    static <V, D> Node<V, D> build(List<V> initialValues,
                                           ValueAggregator<V, Integer> aggregator,
                                           D zeroDelegate,
                                           int tl,
                                           int tr) {
        if (tl + 1 == tr) {
            return new Node<>(initialValues.get(tl), zeroDelegate);
        } else {
            int tm = (tl + tr) / 2;
            return new Node<>(
                    build(initialValues, aggregator, zeroDelegate, tl, tm),
                    build(initialValues, aggregator, zeroDelegate, tm, tr),
                    aggregator,
                    tl, tm, tr,
                    zeroDelegate
            );
        }
    }

    @Override
    protected void pushDown(Node<V, D> t, int tl, int tr) {
        t.setValue(applier.apply(t.getDelegate(), t.getValue(), tl, tr));
        if (tl + 1 < tr) {
            t.getL().setDelegate(composer.apply(t.getL().getDelegate(), t.getDelegate()));
            t.getR().setDelegate(composer.apply(t.getR().getDelegate(), t.getDelegate()));
        }
        t.setDelegate(zeroDelegate);
    }
}
