package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;

public class StringLiteral implements IExpression {
    private Token token;
    private String value;

    public StringLiteral(Token token, String value) {
        this.token = token;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void expressionNode() { }

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public String string() {
        return token.getLiteral();
    }
}
