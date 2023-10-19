package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;

public class IntegerLiteral implements IExpression {
    private Token token;
    private int value;

    public IntegerLiteral(Token token, int value) {
        this.token = token;
        this.value = value;
    }
    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }
    @Override
    public String string() {
        return tokenLiteral();
    }

    public int getValue() {
        return value;
    }
}
