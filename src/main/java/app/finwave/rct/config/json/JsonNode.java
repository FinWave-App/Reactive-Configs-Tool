package app.finwave.rct.config.json;

import app.finwave.rct.config.ConfigNode;
import app.finwave.rct.reactive.property.Property;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Function;

public class JsonNode implements ConfigNode {
    protected Property<JsonObject> object;
    protected Gson gson;

    public JsonNode(Property<JsonObject> object, Gson gson) {
        this.object = object;
        this.gson = gson;
    }

    @Override
    public <T> Property<T> getAs(Class<T> type) {
        return object.map(
                (obj) -> {
                    if (obj == null || obj.isEmpty())
                        return null;

                    T value = null;
                    try {
                        value = gson.fromJson(obj, type);
                    }catch (Exception ignored) {}

                    return value;
                },
                (obj) -> obj == null ? new JsonObject() : gson.toJsonTree(obj, type).getAsJsonObject()
        );
    }

    @Override
    public ConfigNode node(String key) {
        return new JsonNode(
                object.map(
                        (obj) -> {
                            JsonElement element = obj == null ? null : obj.get(key);

                            return element == null ? new JsonObject() : element.getAsJsonObject();
                        },
                        (subObj) -> {
                            JsonObject obj = object.get();
                            obj.add(key, subObj);

                            return obj;
                        }),
                gson
        );
    }

    @Override
    public boolean exists(String key) {
        JsonElement element = object.get();
        if (element == null)
            return false;

        return !element.isJsonNull() && element.isJsonObject() && element.getAsJsonObject().has(key);
    }

    protected <X> X mapElement(JsonObject obj, String key, Function<JsonElement, X> mapper) {
        JsonElement element = obj.get(key);
        if (element == null)
            return null;

        X value = null;

        try {
            value = mapper.apply(element);
        }catch (Exception ignored) {}

        return value;
    }

    @Override
    public Property<String> getAsString(String key) {
        return object.map(
                (obj) -> mapElement(obj, key, JsonElement::getAsString),
                (n) -> {
                    JsonObject obj = object.get();
                    obj.addProperty(key, n);

                    return obj;
                }
        );
    }

    @Override
    public Property<Integer> getAsInteger(String key) {
        return object.map(
                (obj) -> mapElement(obj, key, JsonElement::getAsInt),
                (n) -> {
                    JsonObject obj = object.get();
                    obj.addProperty(key, n);

                    return obj;
                }
        );
    }

    @Override
    public Property<Boolean> getAsBoolean(String key) {
        return object.map(
                (obj) -> mapElement(obj, key, JsonElement::getAsBoolean),
                (n) -> {
                    JsonObject obj = object.get();
                    obj.addProperty(key, n);

                    return obj;
                }
        );
    }

    @Override
    public Property<Float> getAsFloat(String key) {
        return object.map(
                (obj) -> mapElement(obj, key, JsonElement::getAsFloat),
                (n) -> {
                    JsonObject obj = object.get();
                    obj.addProperty(key, n);

                    return obj;
                }
        );
    }

    @Override
    public Property<Double> getAsDouble(String key) {
        return object.map(
                (obj) -> mapElement(obj, key, JsonElement::getAsDouble),
                (n) -> {
                    JsonObject obj = object.get();
                    obj.addProperty(key, n);

                    return obj;
                }
        );
    }

    @Override
    public Property<Long> getAsLong(String key) {
        return object.map(
                (obj) -> mapElement(obj, key, JsonElement::getAsLong),
                (n) -> {
                    JsonObject obj = object.get();
                    obj.addProperty(key, n);

                    return obj;
                }
        );
    }
}
