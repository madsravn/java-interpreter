package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;

public class InfixExpression implements IExpression{
    private Token token;
    private IExpression left;
    private IExpression right;
    private String operator;

    public InfixExpression(Token token, IExpression left, String operator, IExpression right) {
        this.token = token;
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public String getOperator(){
        return operator;
    }

    public IExpression getRight() {
        return right;
    }

    public IExpression getLeft() {
        return left;
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
        sb.append(left.string());
        sb.append(" " + operator + " ");
        sb.append(right.string());
        sb.append(")");

        return sb.toString();
    }
}
