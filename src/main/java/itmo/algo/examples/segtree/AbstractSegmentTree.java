package itmo.algo.examples.segtree;

import itmo.algo.examples.segtree.details.Node;
import itmo.algo.examples.segtree.details.ValueAggregator;
import itmo.algo.examples.segtree.details.DelegateApplier;

import java.util.function.BiFunction;

/**
 * Абстракия для обычногго и неявного дерева отрезков
 * @param <V> Хранимое значение, на которой определена ассоциативная операция (см. aggregator)
 * @param <D> Отложенное вычисление над V. Применяется через applier. Комбинируется через composer.
 * applier.apply(zeroDelegate, value, tl, tr).equals(value) == true
 */
public abstract class AbstractSegmentTree<V, D> {
    protected final int size;
    protected final Node<V, D> root;
    protected final ValueAggregator<V, Integer> aggregator;
    protected final DelegateApplier<D, V, Integer> applier;
    protected final BiFunction<D, D, D> composer;
    protected final D zeroDelegate;

    protected AbstractSegmentTree(int size,
                                Node<V, D> root,
                                ValueAggregator<V, Integer> aggregator,
                                DelegateApplier<D, V, Integer> applier,
                                BiFunction<D, D, D> composer,
                                D zeroDelegate) {
        this.size = size;
        this.root = root;
        this.aggregator = aggregator;
        this.applier = applier;
        this.composer = composer;
        this.zeroDelegate = zeroDelegate;
    }

    public V get(int l, int r) {
        return get(root, l, r, 0, size);
    }

    public void set(int l, int r, D d) {
        set(root, l, r, d, 0, size);
    }

    private V get(Node<V, D> t, int l, int r, int tl, int tr) {
        assert tl <= l && l < r && r <= tr;

        pushDown(t, tl, tr);
        if (tl == l && r == tr) {
            return t.getValue();
        } else {
            int tm = (tl + tr) / 2;
            if (r <= tm) {
                return get(t.getL(), l, r, tl, tm);
            } else if (l >= tm) {
                return get(t.getR(), l, r, tm, tr);
            } else {
                return aggregator.apply(
                        get(t.getL(), l, tm, tl, tm),
                        get(t.getR(), tm, r, tm, tr),
                        tl, tr
                );
            }
        }
    }

    private void set(Node<V, D> t, int l, int r, D d, int tl, int tr) {
        assert tl <= l && l < r && r <= tr;

        if (tl == l && r == tr) {
            t.setDelegate(composer.apply(t.getDelegate(), d));
            pushDown(t, tl, tr);
        } else {
            pushDown(t, tl, tr);
            int tm = (tl + tr) / 2;
            if (r <= tm) {
                set(t.getL(), l, r, d, tl, tm);
                pushDown(t.getR(), tm, tr);
            } else if (l >= tm) {
                pushDown(t.getL(), tl, tm);
                set(t.getR(), l, r, d, tm, tr);
            } else {
                set(t.getL(), l, tm, d, tl, tm);
                set(t.getR(), tm, r, d, tm, tr);
            }
        }
        pullUp(t, tl, tr);
    }

    /**
     * Применяет D к V, проталкивает D вниз.
     * В ленивом варианте создает сыновей при их отсутствии.
     */
    protected abstract void pushDown(Node<V, D> t, int tl, int tr);

    /**
     * Обновляет V из сыновей
     */
    private void pullUp(Node<V, D> t, int tl, int tr) {
        assert t.getL() != null && t.getR() != null;

        t.setValue(aggregator.apply(t.getL().getValue(), t.getR().getValue(), tl, tr));
    }
}
