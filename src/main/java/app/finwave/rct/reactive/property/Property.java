package app.finwave.rct.reactive.property;

import app.finwave.rct.reactive.ChangeListener;
import app.finwave.rct.reactive.value.Value;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Reactive value
 * @param <T> Object type
 */
public interface Property<T> extends Value<T> {

    /**
     * Create new property with typed null value
     * @return New empty property
     */
    static <X> Property<X> create() {
        return of((X) null);
    }

    /**
     * Create new property. See {@link Property#set(Object)}
     * @return New property
     */
    static <X> Property<X> of(X value) {
        var prop = new PropertyImpl<X>();
        prop.set(value);

        return prop;
    }

    /**
     * Create new property by supplier. See {@link Property#set(Value)}
     * @return New property
     */
    static <X> Property<X> of(Value<X> value) {
        var prop = new PropertyImpl<X>();
        prop.set(value);

        return prop;
    }

    /**
     * Create new property by supplier. See {@link Value#dynamic(Supplier, Value[])} of details
     * @return New property
     */
    static <X> Property<X> of(Supplier<X> supplier, Value<?>... dependencies) {
        var prop = new PropertyImpl<X>();
        prop.set(supplier, dependencies);

        return prop;
    }

    /**
     * Create new property by supplier. As {@link Value#dynamic(Supplier, Value[])}, but without dependencies
     * @return New property
     */
    static <X> Property<X> of(Supplier<X> supplier) {
        var prop = new PropertyImpl<X>();
        prop.set(supplier);

        return prop;
    }

    /**
     * @param defaultValue
     * @return Current value. If value is null, method set will be called
     */
    T getOr(T defaultValue);

    /**
     * Follow reactive value. Like {@link Property#set(T)} but with looking at state of the passed {@link Value}
     * @param value reactive value
     */
    void set(Value<T> value);

    /**
     * Set new value. If value is different, change listeners will call
     * @param value new value
     */
    void set(T value);

    /**
     * Follow supplier with dependencies. See {@link Value#dynamic(Supplier, Value[])} of details
     */
    default void set(Supplier<T> supplier, Value<?>... dependencies) {
        set(Value.dynamic(supplier, dependencies));
    }

    /**
     * Two-sided lazy mapping. If the property has a DynamicValue on our side, then the values on the other side will be updated only when we call get().
     * <p>
     * At the same time, DynamicValue can be overwritten with a regular value if we call set() on the other side.
     * <p>
     * If this property is changed, then the value of the child will be set via fromSource function.
     * <p>
     * If child property is changed, then the value of this property will be set via toSource function.
     * @param fromSource Mapping function from source to child property
     * @param toSource Mapping function from child to source property
     * @return Child property
     * @param <X> Child property type
     */
    <X> Property<X> map(Function<T, X> fromSource, Function<X, T> toSource);

    /**
     * Map method as {@link Property#map(Function, Function)} but return property with provided change listener what called only for outside set() call, not because set new value from mapper
     * @param mapper Mapping function from source to child property
     * @param childListener Child changes listener
     * @return Child property
     * @param <X> Child property type
     */
    <X> Property<X> mapWithListener(Function<T, X> mapper, ChangeListener<X> childListener);
}
