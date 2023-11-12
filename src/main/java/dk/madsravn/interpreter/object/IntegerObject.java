package dk.madsravn.interpreter.object;

import java.util.Objects;

public class IntegerObject implements IObject {
    private int value;
    private static String OBJ_TYPE = "INTEGER";

    public IntegerObject(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String inspect() {
        return "" + value;
    }

    // TODO: Create enum for this.
    @Override
    public String type() {
        return OBJ_TYPE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(OBJ_TYPE, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IntegerObject that = (IntegerObject) o;
        return this.value == that.value;
    }

}
