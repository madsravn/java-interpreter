package dk.madsravn.interpreter.object;
//TODO: Maybe rename to ReturnValueObject instead. Contains the value
public class ReturnObject implements IObject {
    private IObject value;
    private static String OBJ_TYPE = "RETURN_VALUE";

    public ReturnObject(IObject value) {
        this.value = value;
    }

    public IObject getValue() {
        return value;
    }
    @Override
    public String type() {
        return OBJ_TYPE;
    }

    @Override
    public String inspect() {
        return value.inspect();
    }

}
