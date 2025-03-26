package app.finwave.rct.config;

import app.finwave.rct.config.json.JsonTransformer;
import com.google.gson.Gson;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JsonConfigTest {

    File tmp;
    Path tmpPath;
    ConfigManager manager;
    ConfigNode rootNode;

    @BeforeAll
    void setup() throws IOException {
        tmp = File.createTempFile("test", ".tmp");
        tmpPath = tmp.toPath();
        tmp.deleteOnExit();
    }

    String readFile() {
        try {
            return Files.readString(tmpPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    void init() throws IOException {
        manager = new ConfigManager(100, TimeUnit.MILLISECONDS);
        rootNode = manager.load(tmp, new JsonTransformer(new Gson())); // as ConfigTypeTransformer.gson, but without pretty printing

        assertNotNull(rootNode);
        assertEquals("", readFile());
    }

    @Test
    @Order(2)
    void defaultValues() {
        var string = rootNode.getAsString("string");
        var integer = rootNode.getAsInteger("integer");
        var bool = rootNode.getAsBoolean("bool");
        var doubleV = rootNode.getAsDouble("double");
        var floatV = rootNode.getAsFloat("float");
        var longV = rootNode.getAsLong("long");

        assertFalse(rootNode.exists("string"));
        assertFalse(rootNode.exists("integer"));
        assertFalse(rootNode.exists("bool"));
        assertFalse(rootNode.exists("double"));
        assertFalse(rootNode.exists("float"));
        assertFalse(rootNode.exists("long"));

        assertNull(string.get());
        assertNull(integer.get());
        assertNull(bool.get());
        assertNull(doubleV.get());
        assertNull(floatV.get());
        assertNull(longV.get());

        assertEquals("", readFile());

        assertEquals("Hello, world!", string.getOr("Hello, world!"));
        assertEquals(123, integer.getOr(123));
        assertEquals(true, bool.getOr(true));
        assertEquals(0.123, doubleV.getOr(0.123));
        assertEquals(0.123f, floatV.getOr(0.123f));
        assertEquals(5290234L, longV.getOr(5290234L));

        assertTrue(rootNode.exists("string"));
        assertTrue(rootNode.exists("integer"));
        assertTrue(rootNode.exists("bool"));
        assertTrue(rootNode.exists("double"));
        assertTrue(rootNode.exists("float"));
        assertTrue(rootNode.exists("long"));

        assertEquals(
                "{\"string\":\"Hello, world!\",\"integer\":123,\"bool\":true,\"double\":0.123,\"float\":0.123,\"long\":5290234}",
                readFile()
        );

        integer.set(555);
        assertEquals(555, integer.get());

        assertEquals(
                "{\"string\":\"Hello, world!\",\"integer\":555,\"bool\":true,\"double\":0.123,\"float\":0.123,\"long\":5290234}",
                readFile()
        );
    }

    @Test
    @Order(3)
    void asClass() {
        var string = rootNode.getAsString("string");
        var integer = rootNode.getAsInteger("integer");
        var bool = rootNode.getAsBoolean("bool");

        var test = rootNode.getAs(TestClass.class);

        assertEquals("Hello, world!", test.get().string);
        assertEquals(555, test.get().integer);
        assertTrue(test.get().bool);

        test.set(new TestClass("Goodbye, world!", 5234, false));

        assertEquals("Goodbye, world!", string.get());
        assertEquals(5234, integer.get());
        assertFalse(bool.get());

        assertEquals("Goodbye, world!", test.get().string);
        assertEquals(5234, test.get().integer);
        assertFalse(test.get().bool);
    }

    @Test
    @Order(4)
    void asClass2() {
        var test2 = rootNode.getAs(TestClass2.class); // simulate "updating" class with new variables

        assertEquals("Another string", test2.get().anotherString);
        assertEquals(999, test2.get().anotherInteger);

        assertEquals(
                "{\"string\":\"Goodbye, world!\",\"integer\":5234,\"bool\":false}",
                readFile()
        );

        var newTest = new TestClass2();
        newTest.anotherString = "Test 123";

        test2.set(newTest);

        assertEquals("Test 123", test2.get().anotherString);
        assertEquals(999, test2.get().anotherInteger);

        assertEquals(
                "{\"anotherString\":\"Test 123\",\"anotherInteger\":999,\"string\":\"Test\",\"integer\":541,\"bool\":false}",
                readFile()
        );
    }

    @Test
    @Order(5)
    void newFromFilesystem() throws IOException, ExecutionException, InterruptedException {
        var test2 = rootNode.getAs(TestClass2.class);

        CompletableFuture<TestClass2> updated = new CompletableFuture<>();
        test2.addChangeListener(updated::complete);

        assertEquals("Test 123", test2.get().anotherString);

        Files.writeString(tmpPath,
                "{\"anotherString\":\"Hello from fs!\",\"anotherInteger\":999,\"string\":\"Test\",\"integer\":541,\"bool\":false}"
        );

        try {
            updated.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            fail("Timed out waiting for updating new value from filesystem");
        }

        assertEquals("Hello from fs!", test2.get().anotherString);
    }

    @Test
    @Order(6)
    void subNode() {
        var test = rootNode.node("sub").getAs(TestClass2.class);
        TestClass2 defaultObj = new TestClass2();
        defaultObj.string = "hello";

        assertNull(test.get());
        assertEquals(defaultObj, test.getOr(defaultObj));

        assertEquals(
                "{\"anotherString\":\"Hello from fs!\",\"anotherInteger\":999,\"string\":\"Test\",\"integer\":541,\"bool\":false," +
                        "\"sub\":{\"anotherString\":\"Another string\",\"anotherInteger\":999,\"string\":\"hello\",\"integer\":541,\"bool\":false}}",
                readFile()
        );
    }

    @Test
    @Order(7)
    void nodeExists() {
        assertTrue(rootNode.exists("anotherString"));
        assertFalse(rootNode.node("test").exists("notExists"));
        assertTrue(rootNode.node("sub").exists("anotherString"));
    }

    static class TestClass {
        String string = "Test";
        int integer = 541;
        boolean bool = false;

        public TestClass() {
        }

        public TestClass(String string, int integer, boolean bool) {
            this.string = string;
            this.integer = integer;
            this.bool = bool;
        }
    }

    static class TestClass2 extends TestClass {
        String anotherString = "Another string";
        int anotherInteger = 999;
    }
}
