package dk.madsravn.interpreter.object;

import java.util.Map;
import java.util.stream.Collectors;

public class HashObject implements IObject {
    private static String OBJ_TYPE = "HASH";
    private Map<IObject, IObject> pairs;

    public HashObject(Map<IObject, IObject> pairs) {
        this.pairs = pairs;
    }

    public Map<IObject, IObject> getPairs() {
        return pairs;
    }
    @Override
    public String type() {
        return OBJ_TYPE;
    }

    @Override
    public String inspect() {
        StringBuilder sb = new StringBuilder();
        String elementsString = pairs.entrySet().stream().map(p -> p.getKey().inspect() + ": " + p.getValue().inspect()).collect(Collectors.joining(", "));
        sb.append("{");
        sb.append(elementsString);
        sb.append("}");

        return sb.toString();
    }
}
