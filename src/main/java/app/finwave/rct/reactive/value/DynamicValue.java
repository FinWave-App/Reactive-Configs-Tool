package app.finwave.rct.reactive.value;

import app.finwave.rct.reactive.ListenerRemover;
import app.finwave.rct.reactive.ChangeListener;
import app.finwave.rct.reactive.InvalidationListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * {@link Value} implementation for lambda functions
 * <p>
 * Change listeners will be called when result of lambda is different then stored.
 */
class DynamicValue<T> implements Value<T> {
    protected Supplier<T> supplier;
    protected T value;

    protected boolean isValid;

    protected Value<?>[] invalidationDependencies;

    protected ArrayList<ChangeListener<T>> changeListeners = new ArrayList<>();
    protected ArrayList<InvalidationListener> invalidationListeners = new ArrayList<>();

    DynamicValue(Supplier<T> supplier, Value<?>... dependencies) {
        this.supplier = supplier;
        this.invalidationDependencies = dependencies;

        Arrays.stream(invalidationDependencies).forEach((d) -> {
            d.addChangeListener((n) -> invalidate());
            d.addInvalidationListener(this::invalidate);
        });
    }

    @Override
    public synchronized T get() {
        if (isValid)
            return value;

        T newValue = supplier.get();

        if (!Objects.equals(value, newValue)) {
            changeListeners.forEach((l) -> l.changed(newValue));

            value = newValue;
        }

        isValid = haveDependencies() && dependenciesIsValid();

        return value;
    }

    protected boolean haveDependencies() {
        return invalidationDependencies != null && invalidationDependencies.length > 0;
    }

    protected boolean dependenciesIsValid() {
        for (Value<?> dependency : invalidationDependencies) {
            if (!dependency.isValid())
                return false;
        }

        return true;
    }

    @Override
    public synchronized void invalidate() {
        if (!isValid)
            return;

        isValid = false;
        invalidationListeners.forEach(InvalidationListener::invalidated);
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public synchronized ListenerRemover addChangeListener(ChangeListener<T> listener) {
        changeListeners.add(listener);

        return () -> changeListeners.remove(listener);
    }

    @Override
    public synchronized ListenerRemover addInvalidationListener(InvalidationListener listener) {
        invalidationListeners.add(listener);

        return () -> invalidationListeners.remove(listener);
    }

    @Override
    public <X> Value<X> map(Function<T, X> mapper) {
        return Value.dynamic(() -> mapper.apply(get()), this);
    }

    @Override
    public String toString() {
        return "DynamicValue{" +
                "value=" + value +
                '}';
    }
}
