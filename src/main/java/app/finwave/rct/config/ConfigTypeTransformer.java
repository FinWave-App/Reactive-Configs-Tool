package app.finwave.rct.config;

import com.google.gson.GsonBuilder;
import app.finwave.rct.config.json.JsonNode;
import app.finwave.rct.config.json.JsonTransformer;
import app.finwave.rct.reactive.property.Property;

/**
 * The implementation of this interface is used to translate from a String to a ConfigNode and back.
 * <p>
 * See sources of {@link JsonTransformer} and {@link JsonNode} for example
 */
public interface ConfigTypeTransformer {
    ConfigTypeTransformer gson = new JsonTransformer(new GsonBuilder().setPrettyPrinting().create());

    ConfigNode transform(Property<String> fileContent);
}
