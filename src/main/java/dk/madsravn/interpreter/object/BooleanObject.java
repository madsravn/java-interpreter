package dk.madsravn.interpreter.object;

import java.util.Objects;

public class BooleanObject implements IObject {
    private boolean value;
    private static String OBJ_TYPE = "BOOLEAN";

    public BooleanObject(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public String type() {
        return OBJ_TYPE;
    }

    public String inspect() {
        return "" + value;
    }
    @Override
    public int hashCode() {
        return Objects.hash(OBJ_TYPE, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BooleanObject that = (BooleanObject) o;
        return this.value == that.value;
    }
}

