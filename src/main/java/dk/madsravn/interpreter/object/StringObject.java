package dk.madsravn.interpreter.object;

import java.util.Objects;

public class StringObject implements IObject {
    private static String OBJ_TYPE = "STRING";
    private String value;

    public StringObject(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    @Override
    public String type() {
        return OBJ_TYPE;
    }

    @Override
    public String inspect() {
        return "\"" + value + "\"";
    }

    @Override
    public int hashCode() {
        return Objects.hash(OBJ_TYPE, value);
    }

    @Override
    public boolean equals(Object o) {
        // TODO: Compare with the one in Token.java
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StringObject that = (StringObject) o;
        return this.value.equals(that.value);
    }
}
