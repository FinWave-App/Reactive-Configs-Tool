package app.finwave.rct.config;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest {

    @Test
    void createWithDefaultTransformer() throws IOException {
        File tmp = new File("./test.json");
        tmp.deleteOnExit();
        ConfigManager configManager = new ConfigManager();
        
        configManager.load(tmp).getAsString("test").set("Hello, world!");
        
        assertEquals(
                "{\n" +
                        "  \"test\": \"Hello, world!\"\n" +
                        "}",
                Files.readString(tmp.toPath())
        );
    }

    @Test
    void unacceptable() throws IOException {
        File tmp = File.createTempFile("test", ".tmp");
        tmp.deleteOnExit();
        assertTrue(tmp.setReadable(false));

        ConfigManager configManager = new ConfigManager();
        ConfigNode node = null;

        try {
            node = configManager.load(tmp, ConfigTypeTransformer.gson);
            fail();
        }catch (IOException e) {}

        assertNull(node);
    }

    @Test
    void unacceptableInRuntime() throws IOException {
        File tmp = File.createTempFile("test", ".tmp");
        tmp.deleteOnExit();

        ConfigManager configManager = new ConfigManager();
        ConfigNode node = configManager.load(tmp, ConfigTypeTransformer.gson);

        assertTrue(tmp.setReadable(false));

        node.getAsString("test").set("Hello, world!");

        assertTrue(tmp.setWritable(false));

        try {
            node.getAsString("test2").set("Hello, world!");
            fail();
        }catch (RuntimeException e) {}
    }

    @Test
    void badListener() throws IOException, ExecutionException, InterruptedException {
        File tmp = File.createTempFile("test", ".tmp");
        tmp.deleteOnExit();
        ConfigManager configManager = new ConfigManager(100, TimeUnit.MILLISECONDS);
        ConfigNode node = configManager.load(tmp, ConfigTypeTransformer.gson);

        var ref = new Object() {
            int exCalls = 0;
            CompletableFuture<Object> future = new CompletableFuture<>();
        };

        var test = node.getAsString("test");
        test.set("Goodbye, world!");

        test.addChangeListener((n) -> {
            ref.exCalls++;
            ref.future.complete(n);
            throw new RuntimeException("Test exception, its ok!");
        });

        Files.writeString(tmp.toPath(), "{\"test\":\"Hello, world!\"}");
        try {
            ref.future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            fail("Timeout while waiting for listener");
        }

        assertEquals(1, ref.exCalls);

        ref.future = new CompletableFuture<>();

        Files.writeString(tmp.toPath(), "{\"test\":\"Test\"}");
        try {
            ref.future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            fail("Timeout while waiting for listener");
        }

        assertEquals(2, ref.exCalls);
    }
}