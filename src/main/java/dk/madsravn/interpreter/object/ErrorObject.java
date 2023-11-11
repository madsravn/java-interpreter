package dk.madsravn.interpreter.object;
public class ErrorObject implements IObject {
    private static String OBJ_TYPE = "ERROR";
    private String message;

    public static ErrorObject unknownOperatorError(String message) {
        return new ErrorObject("unknown operator: " + message);
    }

    public static ErrorObject typeMismatchError(String message) {
        return new ErrorObject("type mismatch: " + message);
    }

    public static ErrorObject identifierNotFoundError(String message) {
        return new ErrorObject("identifier not found: " + message);
    }

    public static ErrorObject wrongNumberOfArguments(int want, int got) {
        return new ErrorObject("wrong number of arguments. got=" + got + ", want=" + want);
    }

    public static ErrorObject argumentNotSupported(String name, String gotType) {
        return new ErrorObject("argument to `" + name + "` not supported, got " + gotType);
    }

    public static ErrorObject indexOperatorNotSupported(String type) {
        return new ErrorObject("index operator not supported: " + type);
    }

    public static ErrorObject argumentToFirstMustBeArray(String type) {
        return new ErrorObject("argument for `first` must be ARRAY, got " + type);
    }

    public static ErrorObject unusableAsHashKey(String type) {
        return new ErrorObject("unusable as hash key: " + type);
    }

    public static ErrorObject notAFunction(String message) {
        return new ErrorObject("not a function: " + message);
    }

    public ErrorObject(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String type() {
        return OBJ_TYPE;
    }

    @Override
    public String inspect() {
        return message;
    }
}
