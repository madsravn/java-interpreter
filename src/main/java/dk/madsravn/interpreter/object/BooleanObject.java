package dk.madsravn.interpreter.object;

public class BooleanObject implements IObject {
    private boolean value;

    public String type() {
        return "BOOLEAN";
    }

    public String inspect() {
        return "" + value;
    }
}

