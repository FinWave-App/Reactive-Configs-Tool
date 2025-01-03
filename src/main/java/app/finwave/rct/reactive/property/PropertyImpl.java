package app.finwave.rct.reactive.property;

import app.finwave.rct.reactive.ChangeListener;
import app.finwave.rct.reactive.InvalidationListener;
import app.finwave.rct.reactive.ListenerRemover;
import app.finwave.rct.reactive.value.Value;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;

class PropertyImpl<T> implements Property<T> {
    protected Value<T> value;
    protected T lastValue;

    protected ListenerRemover followRemover;

    protected boolean isValid;

    protected ArrayList<ChangeListener<T>> changeListeners = new ArrayList<>();
    protected ArrayList<InvalidationListener> invalidationListeners = new ArrayList<>();

    PropertyImpl() {
    }

    @Override
    public synchronized T getOr(T defaultValue) {
        T value = get();

        if (value == null) {
            value = defaultValue;
            set(value);
        }

        return value;
    }

    @Override
    public synchronized void set(Value<T> value) {
        var old = this.value;
        this.value = value;

        if (followRemover != null) {
            followRemover.remove();
            followRemover = null;
        }

        var changeRemover = value.addChangeListener((n) -> invalidate());
        var invalidationRemover = value.addInvalidationListener(this::invalidate);

        followRemover = () -> {
            changeRemover.remove();
            invalidationRemover.remove();
        };

        if (Objects.equals(old, value))
            return;

        invalidate();
    }

    @Override
    public synchronized void set(T value) {
        set(Value.wrap(value));

        lastValue = value;

        changeListeners.forEach((l) -> l.changed(value));
        isValid = true;
    }

    protected void checkChanges() {
        T newValue = value.get();

        if (!Objects.equals(lastValue, newValue)) {
            lastValue = newValue;

            changeListeners.forEach((l) -> l.changed(newValue));
        }

        // Value like DynamicValue can be invalid even after call get() method, so we should call get() method all time
        isValid = value.isValid();
    }

    @Override
    public <X> Property<X> map(Function<T, X> fromSource, Function<X, T> toSource) {
        DoubleChangeObserver observer = new DoubleChangeObserver();

        Property<X> child = Property.of(fromSource.apply(get()));

        addChangeListener(
                (n) -> {
                    if (!observer.tryUpdate()) return;

                    try {
                        child.set(fromSource.apply(n));
                    }finally {
                        observer.unlock();
                    }

                }
        );

        child.addChangeListener(
                (n) -> {
                    if (!observer.tryUpdate()) return;

                    try {
                        set(toSource.apply(n));
                    }finally {
                        observer.unlock();
                    }
                }
        );

        return child;
    }

    @Override
    public <X> Property<X> mapWithListener(Function<T, X> fromSource, ChangeListener<X> listener) {
        DoubleChangeObserver observer = new DoubleChangeObserver();

        Property<X> child = Property.of(fromSource.apply(get()));

        addChangeListener(
                (n) -> {
                    if (!observer.tryUpdate()) return;

                    try {
                        child.set(fromSource.apply(n));
                    }finally {
                        observer.unlock();
                    }

                }
        );

        child.addChangeListener(
                (n) -> {
                    if (!observer.tryUpdate()) return;

                    try {
                        listener.changed(n);
                    }finally {
                        observer.unlock();
                    }
                }
        );

        return child;
    }

    @Override
    public synchronized T get() {
        if (isValid)
            return lastValue;

        checkChanges();

        return lastValue;
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
        return "PropertyImpl{" +
                "lastValue=" + lastValue +
                '}';
    }
}

class DoubleChangeObserver {
    protected boolean otherUpdate;

    public boolean tryUpdate() {
        return !otherUpdate && (otherUpdate = true);
    }

    public void unlock() {
        otherUpdate = false;
    }
}
