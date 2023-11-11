package dk.madsravn.interpreter.object;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class BuiltinFunctionObject implements IObject {
    private static String OBJ_TYPE = "BUILTIN";
    private Function<List<IObject>, IObject> func;

    public BuiltinFunctionObject(Function<List<IObject>, IObject> func) {
        this.func = func;
    }

    @Override
    public String type() {
        return OBJ_TYPE;
    }

    @Override
    public String inspect() {
        return "builtin function";
    }

    public IObject apply(List<IObject> objects) {
        return func.apply(objects);
    }
}
