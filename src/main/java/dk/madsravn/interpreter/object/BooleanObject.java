package dk.madsravn.interpreter.object;

public class BooleanObject implements IObject {
    private boolean value;
    private static String OBJ_TYTPE = "BOOLEAN";

    public BooleanObject(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public String type() {
        return OBJ_TYTPE;
    }

    public String inspect() {
        return "" + value;
    }
}

