package dk.madsravn.interpreter.object;

public class BooleanObject implements IObject {
    private boolean value;

    public BooleanObject(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public String type() {
        return "BOOLEAN";
    }

    public String inspect() {
        return "" + value;
    }
}

