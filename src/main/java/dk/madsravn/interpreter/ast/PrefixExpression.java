package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;

public class PrefixExpression implements IExpression{
    private Token token;
    private String operator;
    private IExpression right;

    public PrefixExpression(Token token, String operator, IExpression right) {
        this.token = token;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public String string() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(operator);
        sb.append(right.string());
        sb.append(")");
        return sb.toString();
    }

    public IExpression getRight() {
        return right;
    }

    public String getOperator() {
        return operator;
    }
}
