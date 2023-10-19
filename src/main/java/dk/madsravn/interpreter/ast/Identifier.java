package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;
import dk.madsravn.interpreter.tokens.TokenType;

public class Identifier implements IExpression {
    private Token token;
    private String value;

    public Identifier(Token token, String value) {
        this.token = token;
        this.value = value;
    }
    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String string() {
        return value;
    }
}
