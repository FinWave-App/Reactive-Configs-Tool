package app.finwave.rct.reactive.property;

import app.finwave.rct.reactive.value.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PropertyTest {

    @Test
    void create() {
        var empty = Property.create();

        assertNull(empty.get());
    }

    @Test
    void testOf() {
        var hw = Property.of("Hello, World!");

        assertEquals("Hello, World!", hw.get());
    }

    @Test
    void testOf1() {
        Property<String> hl = Property.of(() -> "Hello, Lambda!");

        assertEquals("Hello, Lambda!", hl.get());
    }

    @Test
    void testOf2() {
        Property<String> hw = Property.of(Value.wrap("Hello, World!"));

        assertEquals("Hello, World!", hw.get());
    }

    @Test
    void addListeners() {
        var ref = new Object() {
            Boolean valid;
            Boolean changed;
        };

        var s1 = Property.of("Hello, World!");
        var s2 = Property.of("Hello, Lambda!");

        var isSame = Property.of(
                () -> s1.get().equals(s2.get()),
                s1, s2
        );

        isSame.addChangeListener((n) -> ref.changed = true);
        isSame.addInvalidationListener(() -> ref.valid = false);

        assertNull(ref.changed);
        assertNull(ref.valid);

        isSame.get();

        assertTrue(ref.changed);
        assertNull(ref.valid);

        s1.set("Goodbye, World!");
        assertFalse(ref.valid);

        isSame.get();
        assertTrue(isSame.isValid());
    }

    @Test
    void setWithValue() {
        Property<String> property = Property.of("Initial Value");

        var ref = new Object() {
            boolean changed = false;
        };

        property.addChangeListener((newValue) -> ref.changed = true);

        property.set("New Value");

        assertEquals("New Value", property.get());
        assertTrue(ref.changed);
    }

    @Test
    void follow() {
        var s1 = Property.of("Hello, World!");
        var s2 = Property.of(() -> "Hello, Lambda!");

        var isSame = Property.of(
                () -> s1.get().equals(s2.get()),
                s1, s2
        );

        assertFalse(isSame.get());

        s1.set("Test 123");

        assertFalse(isSame.isValid());
        assertFalse(isSame.get());

        s2.set("Test 123");

        assertTrue(isSame.get());
    }

    @Test
    void invalidate() {
        var ref = new Object() {
            Boolean valid;
        };

        var s1 = Property.of("Hello, World!");
        var s2 = Property.of("Hello, Lambda!");

        var isSame = Property.of(
                () -> s1.get().equals(s2.get()),
                s1, s2
        );
        var isSameString = Property.of(() -> "is same? " + isSame.get(), isSame);
        isSameString.get();

        isSameString.addInvalidationListener(() -> ref.valid = false);

        assertNull(ref.valid);

        isSame.invalidate();

        assertFalse(ref.valid);
    }

    @Test
    void createWithTypedNull() {
        Property<String> property = Property.create();

        assertNull(property.get());

        property.set("Non-null Value");
        assertEquals("Non-null Value", property.get());
    }

    @Test
    void map() {
        Property<Integer> baseValue = Property.of(10);

        Value<String> mappedValue = baseValue.map(n -> n * 2)
                .map(n -> "Doubled: " + n)
                .map(s -> s + "!");

        assertEquals("Doubled: 20!", mappedValue.get());

        baseValue.set(15);
        assertEquals("Doubled: 30!", mappedValue.get());
    }

    @Test
    void mapWithListener() {
        var ref = new Object() {
            int calls = 0;
        };

        Property<Integer> baseValue = Property.of(10);
        Property<String> baseString = baseValue.mapWithListener(String::valueOf, (string) -> ref.calls++);

        assertEquals("10", baseString.get());
        assertEquals(0, ref.calls);

        baseString.set("20");
        assertEquals("20", baseString.get());
        assertEquals(10, baseValue.get());
        assertEquals(1, ref.calls);

        baseValue.set(30);
        assertEquals(30, baseValue.get());
        assertEquals("30", baseString.get());
        assertEquals(1, ref.calls);
    }

    @Test
    void map2Sided() {
        var ref = new Object() {
            int toStringCalls = 0;
            int toIntCalls = 0;
        };

        Property<Integer> x = Property.of(10);
        Property<String> doubledX = x.map(
                (v) -> {
                    ref.toStringCalls++;
                    return String.valueOf(v * 2);
                },
                (v) -> {
                    ref.toIntCalls++;
                    return Integer.parseInt(v) / 2;
                }
        );

        assertEquals(10, x.get());
        assertEquals("20", doubledX.get());

        assertEquals(1, ref.toStringCalls);
        assertEquals(0, ref.toIntCalls);


        x.set(20);
        assertEquals("40", doubledX.get());
        assertEquals(20, x.get());

        assertEquals(2, ref.toStringCalls);
        assertEquals(0, ref.toIntCalls);


        doubledX.set("10");
        assertEquals(5, x.get());

        assertEquals(2, ref.toStringCalls);
        assertEquals(1, ref.toIntCalls);
    }

    @Test
    void map2SidedWrong() {
        var ref = new Object() {
            int toStringCalls = 0;
            int toIntCalls = 0;
        };

        Property<Integer> x = Property.of(10);
        Property<String> doubledX = x.map(
                (v) -> {
                    ref.toStringCalls++;
                    return String.valueOf(v * 2);
                },
                (v) -> {
                    ref.toIntCalls++;
                    return Integer.parseInt(v); // dont split
                }
        );

        assertEquals(10, x.get());
        assertEquals("20", doubledX.get());

        assertEquals(1, ref.toStringCalls);
        assertEquals(0, ref.toIntCalls);


        x.set(20);
        assertEquals("40", doubledX.get());
        assertEquals(20, x.get());

        assertEquals(2, ref.toStringCalls);
        assertEquals(0, ref.toIntCalls);

        doubledX.set("10");
        assertEquals(10, x.get());
        assertEquals("10", doubledX.get());

        assertEquals(2, ref.toStringCalls);
        assertEquals(1, ref.toIntCalls);
    }

    @Test
    void map2SidedDynamic() {
        var ref = new Object() {
            int raw = 0;
        };

        Property<Integer> x = Property.of(() -> ref.raw);
        Property<String> y = x.map(
                String::valueOf,
                Integer::parseInt
        );

        assertEquals(0, x.get());
        assertEquals("0", y.get());

        ref.raw++;
        assertEquals(1, x.get());
        assertEquals("1", y.get());

        ref.raw++;
        assertEquals("1", y.get());
        assertEquals(2, x.get());


        y.set("12345");
        assertEquals(12345, x.get());

        ref.raw++;
        assertEquals(12345, x.get());
    }

    @Test
    void defaultValue() {
        var prop = Property.<Integer>create();

        assertNull(prop.get());
        assertEquals(0, prop.getOr(0));
        assertEquals(0, prop.get());

        assertEquals(0, prop.getOr(5));
        assertEquals(0, prop.get());
    }

    @Test
    void testToString() {
        assertEquals("PropertyImpl{lastValue=123}", Property.of(123).toString());
        assertEquals("PropertyImpl{lastValue=null}", Property.of(() -> 123).toString());
    }
}