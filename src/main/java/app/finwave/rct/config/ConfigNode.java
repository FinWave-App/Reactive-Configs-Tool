package app.finwave.rct.config;

import app.finwave.rct.reactive.property.Property;

public interface ConfigNode {

    /**
     * Provide this node values as object.
     * Even if the reactive values from getAs*() have changed, the property from this method will provide a new object.
     * <p>
     * Warning: for save changed values in object you must call set() on property
     */
    <T> Property<T> getAs(Class<T> type);

    /**
     * Provide reactive string value from this node.
     */
    Property<String> getAsString(String key);

    /**
     * Provide reactive int value from this node.
     */
    Property<Integer> getAsInteger(String key);

    /**
     * Provide reactive boolean value from this node.
     */
    Property<Boolean> getAsBoolean(String key);

    /**
     * Provide reactive float value from this node.
     */
    Property<Float> getAsFloat(String key);

    /**
     * Provide reactive double value from this node.
     */
    Property<Double> getAsDouble(String key);

    /**
     * Provide reactive long value from this node.
     */
    Property<Long> getAsLong(String key);

    /**
     * Provide subnode from this node.
     */
    ConfigNode node(String key);

    /**
     * Check key is existing.
     */
    boolean exists(String key);
}
