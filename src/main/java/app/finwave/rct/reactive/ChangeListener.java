package app.finwave.rct.reactive;

public interface ChangeListener<T> {
    void changed(T newValue);
}
