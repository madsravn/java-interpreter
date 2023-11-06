package dk.madsravn.interpreter.object;

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
        return value;
    }
}
