package app.finwave.rct.reactive.value;

import app.finwave.rct.reactive.ListenerRemover;
import app.finwave.rct.reactive.ChangeListener;
import app.finwave.rct.reactive.InvalidationListener;

import java.util.Objects;
import java.util.function.Function;

/**
 * {@link Value} implementation. Just store object.
 * <p>
 * Change listeners will never be called, because value always is same.
 * Invalidation listeners will never be called, because value always is valid.
 */
class StaticValue<T> implements Value<T> {
    protected T value;

    StaticValue(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void invalidate() {

    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public ListenerRemover addChangeListener(ChangeListener<T> listener) {
        return ListenerRemover.VOID;
    }

    @Override
    public ListenerRemover addInvalidationListener(InvalidationListener listener) {
        return ListenerRemover.VOID;
    }

    @Override
    public <X> Value<X> map(Function<T, X> mapper) {
        return new StaticValue<>(mapper.apply(value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaticValue<?> that = (StaticValue<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public String toString() {
        return "StaticValue{" +
                "value=" + value +
                '}';
    }
}
