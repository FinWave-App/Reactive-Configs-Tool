package app.finwave.rct.reactive.value;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StaticValueTest {

    @Test
    void get() {
        String testString = "test";
        Value<String> value = Value.wrap(testString);

        assertEquals(testString, value.get());
    }

    @Test
    void map() {
        String testString = "Hello World!";
        Value<String> value = Value.wrap(testString);
        Value<Integer> stringLength = value.map(String::length);

        assertEquals(testString.length(), stringLength.get());
    }

    @Test
    void testEquals() {
        Value<String> value1 = Value.wrap("test");
        Value<String> value2 = Value.wrap("test");
        Value<String> value3 = Value.wrap("different");

        assertEquals(value1, value2);
        assertNotEquals(value1, value3);
        assertNotEquals(null, value1);
    }

    // Just for coverage

    @Test
    void invalidate() {
        String testString = "test";
        Value<String> value = Value.wrap(testString);

        value.invalidate();
    }

    @Test
    void isValid() {
        String testString = "test";
        Value<String> value = Value.wrap(testString);
        assertTrue(value.isValid());
    }

    @Test
    void addChangeListener() {
        String testString = "test";
        Value<String> value = Value.wrap(testString);
        var remover = value.addChangeListener((n) -> {});

        remover.remove();
    }

    @Test
    void addInvalidationListener() {
        String testString = "test";
        Value<String> value = Value.wrap(testString);
        var remover = value.addInvalidationListener(() -> {});

        remover.remove();
    }

    @Test
    void testEquals1() {
        var value1 = Value.wrap(new String("test"));
        var value2 = Value.wrap(new String("test"));

        assertEquals(value1, value2);
    }

    @Test
    void testToString() {
        var value = Value.wrap(123);
        assertEquals("StaticValue{value=123}", value.toString());
    }
}
