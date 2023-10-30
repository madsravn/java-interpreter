package dk.madsravn.interpreter.object;

public class NullObject implements IObject {
    @Override
    public String type() {
        return "NULL";
    }

    @Override
    public String inspect() {
        return "null";
    }

}
