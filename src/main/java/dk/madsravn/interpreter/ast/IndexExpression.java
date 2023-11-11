package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;

public class IndexExpression implements IExpression {
    private Token token;
    private IExpression left;
    private IExpression index;

    public IndexExpression(Token token, IExpression left, IExpression index) {
        this.token = token;
        this.left = left;
        this.index = index;
    }

    public IExpression getLeft() {
        return left;
    }

    public IExpression getIndex() {
        return index;
    }

    @Override
    public void expressionNode() {  }

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public String string() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(left.string());
        sb.append("[");
        sb.append(index.string());
        sb.append("])");

        return sb.toString();
    }
}
