package app.finwave.rct.reactive.value;

import app.finwave.rct.reactive.ChangeListener;
import app.finwave.rct.reactive.InvalidationListener;
import app.finwave.rct.reactive.ListenerRemover;
import app.finwave.rct.reactive.property.Property;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Read-only reactive value
 * @param <T> Object type
 */
public interface Value<T> {
    Value<?> EMPTY = Value.wrap(null);

    /**
     * @return {@link StaticValue} implementation
     */
    static <T> Value<T> wrap(T object) {
        return new StaticValue<>(object);
    }

    /**
     * Sometimes we need to combine few Values into one, and at the same time listen for changes in each of them.
     * This method allows you to pass a regular Supplier with the dependent Values.
     * <p>
     * If dependencies is empty or contains {@link DynamicValue} without dependencies (in other words, dependencies that can never be valid.)
     * then dependency tracking doesn't make sense: value will get new values from supplier each time when get() called and will never be valid
     * @param supplier Supplier function
     * @param dependencies Invalidation dependencies
     */
    static <T> Value<T> dynamic(Supplier<T> supplier, Value<?>... dependencies) {
        return new DynamicValue<>(supplier, dependencies);
    }

    /**
     * @return Current value. Can be null
     */
    T get();

    /**
     * Invalidate current value
     * <p>
     * When value is invalid the next {@link Value#get()} call will update the internal state of the value if it makes sense.
     * As example, {@link StaticValue} do nothing, but followed {@link Property} call parent for get new value
     */
    void invalidate();

    /**
     * @return validation status of this value
     */
    boolean isValid();

    /**
     * @param listener Listener to add. Listener is called when current value change detected
     * @return {@link ListenerRemover}, which removes the passed listener
     */
    ListenerRemover addChangeListener(ChangeListener<T> listener);

    /**
     * @param listener Listener to add. Listener is called when current value is invalid
     * @return {@link ListenerRemover}, which removes the passed listener
     */
    ListenerRemover addInvalidationListener(InvalidationListener listener);

    /**
     * @param mapper Mapper function
     * @param <X> New type
     * @return New {@link Value}, what apply mapper every time when value changes
     */
    <X> Value<X> map(Function<T, X> mapper);
}
