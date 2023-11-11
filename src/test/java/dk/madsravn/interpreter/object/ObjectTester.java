package dk.madsravn.interpreter.object;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectTester {

    @Test
    public void testStringHashKey() {
        var hello1 = new StringObject("Hello world");
        var hello2 = new StringObject("Hello world");

        var diff1 = new StringObject("My name is johnny");
        var diff2 = new StringObject("My name is johnny");
        assertEquals(hello1.hashCode(), hello2.hashCode());
        assertEquals(diff1.hashCode(), diff2.hashCode());
        assertNotEquals(hello1.hashCode(), diff1.hashCode());
        assertNotEquals(hello2.hashCode(), diff2.hashCode());
    }

    //TODO: Create tests for equality and hashCode of objects of type BooleanObject, IntegerObject and StringObject
}
