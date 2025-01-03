package app.finwave.rct.config;

import app.finwave.rct.reactive.property.Property;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Config Manager is used to load config files, monitor and write changes.
 * <p>
 * Example:
 * <pre>
 *     {@code
 *     void helloWorldFromConfig() throws IOException {
 *         ConfigManager configManager = new ConfigManager();
 *         ConfigNode config = configManager.load(new File("./config.json"), ConfigTypeTransformer.gson);
 *
 *         Property<String> toPrint = config.getAsString("textToPrint");
 *         System.out.println(toPrint.getOr("Hello World!"));
 *     }
 *     }
 * </pre>
 * This example creates (if it does not exist) and loads config.json file with tracking changes from outside,
 * <p>
 * get String "textToPrint" and print it.
 * <p>
 * If the config is empty or just does not provide this variable, "Hello World!" is used, and it is written to this file
 */
public class ConfigManager {
    protected FileWatcher watcher;

    /**
     * Creates new ConfigManager what track config files changes from the filesystem every second
     * @throws IOException If an I/O error occurs
     */
    public ConfigManager() throws IOException {
        this(1, TimeUnit.SECONDS);
    }

    /**
     * Creates new ConfigManager with specific period of tracking config files changes from the filesystem
     * @throws IOException If an I/O error occurs
     */
    public ConfigManager(long watchPeriod, TimeUnit timeUnit) throws IOException {
        this.watcher = new FileWatcher(watchPeriod, timeUnit);
    }

    protected Property<String> loadString(File file) throws IOException {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            file.createNewFile();
        }

        Property<String> fileContent = Property.create();

        fileContent.set(Files.readString(file.toPath()));
        watcher.watch(file, () -> {
            String content = null;

            try {
                content = Files.readString(file.toPath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (!Objects.equals(content, fileContent.get()))
                fileContent.set(content);
        });

        return fileContent.map((from) -> from, (to) -> { // watch fileContent updates from code
            if (!Objects.equals(to, fileContent.get())) {
                try {
                    Files.writeString(file.toPath(), to);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return to;
        });
    }

    /**
     * Provide root {@link ConfigNode} from file.
     * If the file does not exist, a new one will be created, including non-existent folders for this file.
     * <p>
     * Default {@link ConfigTypeTransformer#gson} will be used (call {@link ConfigManager#load(File, ConfigTypeTransformer)} with custom {@link ConfigTypeTransformer} for a different behavior)
     * @param file Target file
     * @throws IOException If file cannot be created, read or other errors with IO
     */
    public ConfigNode load(File file) throws IOException {
        return load(file, ConfigTypeTransformer.gson);
    }

    /**
     * Provide root {@link ConfigNode} from file.
     * If the file does not exist, a new one will be created, including non-existent folders for this file.
     * @param file Target file
     * @param transformer Config transformer, what defines how values will write or read.
     * @throws IOException If file cannot be created, read or other errors with IO
     */
    public ConfigNode load(File file, ConfigTypeTransformer transformer) throws IOException {
        Property<String> fileContent = loadString(file);

        return transformer.transform(fileContent);
    }
}
