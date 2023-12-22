package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;

import java.lang.reflect.Array;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayLiteral implements IExpression {
    private Token token;
    private List<IExpression> elements;

    public ArrayLiteral(Token token, List<IExpression> elements) {
        this.token = token;
        this.elements = elements;
    }

    public int getElementsLength() {
        return elements.size();
    }

    public List<IExpression> getElements() {
        return elements;
    }

    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public String string() {
        String elementsString = elements.stream().map(p -> p.string()).collect(Collectors.joining(", "));
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(elementsString);
        sb.append("]");

        return sb.toString();
    }

}
