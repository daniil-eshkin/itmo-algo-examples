package itmo.algo.examples.segtree;

import itmo.algo.examples.segtree.details.ValueAggregator;
import lombok.Data;

import java.util.List;

// Чтоб прошлый пример не дал ощущение, будто персистентное ДО - это сложно
public class PersistentSegTreeWithoutDelegate<V> {
    private final int size;
    private final Node<V> root;
    private final ValueAggregator<V, Integer> aggregator;

    private PersistentSegTreeWithoutDelegate(int size, Node<V> root, ValueAggregator<V, Integer> aggregator) {
        this.size = size;
        this.root = root;
        this.aggregator = aggregator;
    }

    public PersistentSegTreeWithoutDelegate(List<V> initialValues,
                              ValueAggregator<V, Integer> aggregator) {
        this(
                initialValues.size(),
                build(
                        initialValues,
                        aggregator,
                        0,
                        initialValues.size()
                ),
                aggregator
        );
    }

    public V get(int l, int r) {
        return get(root, l, r, 0, size);
    }

    public PersistentSegTreeWithoutDelegate<V> set(int pos, V newVal) {
        return new PersistentSegTreeWithoutDelegate<>(
                size,
                set(root, pos, newVal, 0, size),
                aggregator
        );
    }

    private V get(Node<V> t, int l, int r, int tl, int tr) {
        assert tl <= l && l < r && r <= tr;

        if (tl == l && r == tr) {
            return t.value;
        } else {
            int tm = (tl + tr) / 2;
            if (r <= tm) {
                return get(t.l, l, r, tl, tm);
            } else if (l >= tm) {
                return get(t.r, l, r, tm, tr);
            } else {
                return aggregator.apply(
                        get(t.l, l, tm, tl, tm),
                        get(t.r, tm, r, tm, tr),
                        l, tm, r
                );
            }
        }
    }

    private Node<V> set(Node<V> t, int pos, V newVal, int tl, int tr) {
        assert tl <= pos && pos < tr;

        if (tl + 1 == tr) {
            return new Node<>(newVal);
        } else {
            int tm = (tl + tr) / 2;
            if (pos < tm) {
                return new Node<>(
                        set(t.l, pos, newVal, tl, tm),
                        t.r,
                        aggregator,
                        tl, tm, tr
                );
            } else {
                return new Node<>(
                        t.l,
                        set(t.r, pos, newVal, tm, tr),
                        aggregator,
                        tl, tm, tr
                );
            }
        }
    }

    static <V> Node<V> build(List<V> initialValues,
                                ValueAggregator<V, Integer> aggregator,
                                int tl,
                                int tr) {
        if (tl + 1 == tr) {
            return new Node<>(initialValues.get(tl));
        } else {
            int tm = (tl + tr) / 2;
            return new Node<>(
                    build(initialValues, aggregator, tl, tm),
                    build(initialValues, aggregator, tm, tr),
                    aggregator,
                    tl, tm, tr
            );
        }
    }

    @Data
    private static class Node<V> {
        private final V value;
        private final Node<V> l;
        private final Node<V> r;

        public Node(V value) {
            this.value = value;
            l = null;
            r = null;
        }

        public Node(Node<V> l, Node<V> r, ValueAggregator<V, Integer> aggregator, int tl, int tm, int tr) {
            this.value = aggregator.apply(l.value, r.value, tl, tm, tr);
            this.l = l;
            this.r = r;
        }
    }
}
