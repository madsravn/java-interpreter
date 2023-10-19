package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;

public class ExpressionStatement implements IStatement {
    Token token;
    IExpression expression;

    public ExpressionStatement(Token token) {
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
        if (expression != null) {
            sb.append(expression.string());
        }
        return sb.toString();
    }

    public void setExpression(IExpression expression) {
        this.expression = expression;
    }

    public IExpression getExpression() {
        return expression;
    }
}
