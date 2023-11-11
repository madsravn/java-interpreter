package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;

import java.util.Map;
import java.util.stream.Collectors;

public class HashLiteral implements IExpression {

    private Token token;
    private Map<IExpression, IExpression> pairs;

    public HashLiteral(Token token, Map<IExpression, IExpression> pairs) {
        this.token = token;
        this.pairs = pairs;
    }

    public Map<IExpression, IExpression> getPairs() {
        return pairs;
    }

    public int getPairsLength() {
        return pairs.size();
    }

    @Override
    public void expressionNode() { }

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public String string() {
        StringBuilder sb = new StringBuilder();
        String content = pairs.entrySet().stream().map(e -> e.getKey().string() + ": " + e.getValue().string()).collect(Collectors.joining(", "));
        sb.append("{");
        sb.append(content);
        sb.append("}");

        return sb.toString();
    }
}
