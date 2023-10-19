package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;

public class ReturnStatement implements IStatement{
    private Token token;
    private IExpression expression;

    public ReturnStatement(Token token) {
        this.token = token;
    }

    @Override
    public void statementNode() {}

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public String string() {
        StringBuilder sb = new StringBuilder();
        sb.append(tokenLiteral() + " ");

        if (expression != null) {
            sb.append(expression.string());
        }

        sb.append(";");

        return sb.toString();
    }

}
