package app.finwave.rct.reactive.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import app.finwave.rct.reactive.property.Property;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class DynamicValueTest {
    Value<Long> i;

    @BeforeEach
    void setUp() {
        i = Value.dynamic(new Supplier<>() {
            long value = -1;

            @Override
            public Long get() {
                value++;

                return value;
            }
        });
    }

    @Test
    void get() {
        assertEquals(0, i.get());
        assertEquals(1, i.get());
    }

    @Test
    void addChangeListener() {
        var ref = new Object() {
            Long lastValue = null;
        };

        i.addChangeListener((newV) -> {
            ref.lastValue = newV;
        });

        i.get();
        assertEquals(0, ref.lastValue);

        i.get();
        assertEquals(1, ref.lastValue);
    }

    @Test
    void map() {
        Value<String> mapped = i.map(String::valueOf);

        assertEquals("0", mapped.get());
        assertEquals("1", mapped.get());
    }

    @Test
    void invalidate() {
        Property<Integer> dependency = Property.of(10);
        Value<Integer> dynamicValueWithDependency = Value.dynamic(() -> dependency.get() * 2, dependency);

        assertEquals(20, dynamicValueWithDependency.get());
        dependency.set(15);

        assertFalse(dynamicValueWithDependency.isValid());
        assertEquals(30, dynamicValueWithDependency.get());
    }

    @Test
    void isValid() {
        assertEquals(0, i.get());
        assertFalse(i.isValid());

        var value = Value.dynamic(() -> String.valueOf(52566234).getBytes(), Value.wrap("52566234"));

        assertFalse(value.isValid());
        var fistCall = value.get();
        assertTrue(value.isValid());
        assertSame(fistCall, value.get());
    }

    @Test
    void testToString() {
        assertEquals("DynamicValue{value=null}", i.toString());

        assertEquals(0, i.get());
        assertEquals("DynamicValue{value=0}", i.toString());
    }
}