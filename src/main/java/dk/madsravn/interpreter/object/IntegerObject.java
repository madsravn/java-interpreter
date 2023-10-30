package dk.madsravn.interpreter.object;

public class IntegerObject implements IObject {
    private int value;

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
        return "INTEGER";
    }

}
