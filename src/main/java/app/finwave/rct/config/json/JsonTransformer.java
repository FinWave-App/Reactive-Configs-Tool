package app.finwave.rct.config.json;

import app.finwave.rct.config.ConfigTypeTransformer;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import app.finwave.rct.config.ConfigNode;
import app.finwave.rct.reactive.property.Property;

public class JsonTransformer implements ConfigTypeTransformer {
    protected Gson gson;

    public JsonTransformer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public ConfigNode transform(Property<String> fileContent) {
        Property<JsonObject> json = fileContent.map(
                (s) -> {
                    if (s == null || s.isBlank())
                        return new JsonObject();

                    JsonObject obj = null;
                    try {
                        obj = gson.fromJson(s, JsonElement.class).getAsJsonObject();
                    }catch (Exception ignored) {} // its normal if json is not valid

                    return obj == null ? new JsonObject() : obj;
                },
                (o) -> gson.toJson(o)
        );

        return new JsonNode(json, gson);
    }
}
