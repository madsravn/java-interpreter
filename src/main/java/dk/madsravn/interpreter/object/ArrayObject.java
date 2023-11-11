package dk.madsravn.interpreter.object;

import java.util.List;
import java.util.stream.Collectors;

public class ArrayObject implements IObject {
    private static String OBJ_TYPE = "ARRAY";
    private List<IObject> elements;

    public ArrayObject(List<IObject> elements) {
        this.elements = elements;
    }

    public int getElementsLength() {
        return elements.size();
    }

    public List<IObject> getElements() {
        return elements;
    }
    @Override
    public String type() {
        return OBJ_TYPE;
    }

    @Override
    public String inspect() {
        StringBuilder sb = new StringBuilder();
        String elementsString = elements.stream().map(p -> p.inspect()).collect(Collectors.joining(", "));
        sb.append("[");
        sb.append(elementsString);
        sb.append("]");

        return sb.toString();
    }
}
