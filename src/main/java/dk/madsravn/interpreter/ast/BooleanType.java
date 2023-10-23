package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;

public class BooleanType implements IExpression {
    private Token token;
    private boolean value;

    public BooleanType(Token token, boolean value) {
        this.token = token;
        this.value = value;
    }
    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String string() {
        return token.getLiteral();
    }
}
