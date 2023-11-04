package dk.madsravn.interpreter.object;

public class NullObject implements IObject {
    private static String OBJ_TYPE = "NULL";
    @Override
    public String type() {
        return "NULL";
    }

    @Override
    public String inspect() {
        return OBJ_TYPE;
    }

}
