# Reactive Configs Tool

This library provides a reactive approach to working with configuration files in Java. It simplifies the process of loading, monitoring, and updating configuration settings in real-time, making it ideal for applications that require reactions to configuration changes without restarting.

## Features

- Reactive Properties: Access configuration values as reactive properties that automatically update when the underlying file changes.
- Type Safety: Retrieve configuration values with type safety using methods like getAsString, getAsInteger, etc.
- Automatic File Watching: Automatically monitors changes to configuration files and updates properties accordingly.
- Flexibility: Work with json configurations via [Gson](https://github.com/google/gson) is implemented, but the architecture allows you to write your own transformer for other config formats.

## Getting Started

### Installation

```gradle
repositories {
    mavenCentral()
    maven {
        url "https://nexus.finwave.app/repository/maven-public"
    }
}

dependencies {
    implementation 'app.finwave.rct:rct:1.0.0'
}
```

### Usage Example

Here's a simple example demonstrating how to use the library:

```java
import app.finwave.rct.config.ConfigManager;
import app.finwave.rct.config.ConfigNode;
import app.finwave.rct.reactive.property.Property;

import java.io.File;
import java.io.IOException;

public class Example {
public static void main(String[] args) throws IOException {
        // Create a ConfigManager instance
        ConfigManager configManager = new ConfigManager();

        // Load the configuration from a JSON file
        ConfigNode config = configManager.load(new File("./config.json"));

        // Access a string property reactively
        Property<String> textToPrint = config.getAsString("textToPrint");

        // Print the value (or default to "Hello World!" and save it)
        System.out.println(textToPrint.getOr("Hello World!"));
    }
}
```

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.