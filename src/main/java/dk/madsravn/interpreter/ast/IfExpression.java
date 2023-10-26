package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;

public class IfExpression implements IExpression {
    private Token token;
    private IExpression condition;
    private BlockStatement consequence;
    private BlockStatement alternative;

    public IfExpression(Token token, IExpression condition, BlockStatement consequence, BlockStatement alternative) {
        this.token = token;
        this.condition = condition;
        this.consequence = consequence;
        this.alternative = alternative;
    }

    public IExpression getCondition() {
        return condition;
    }

    public BlockStatement getConsequence() {
        return consequence;
    }

    public BlockStatement getAlternative() {
        return alternative;
    }

    @Override
    public void expressionNode() {}

    @Override
    public String string() {
        StringBuilder sb = new StringBuilder();
        sb.append("if ");
        sb.append(condition.string());
        sb.append(" ");
        sb.append(consequence.string());
        if (alternative != null) {
            sb.append("else ");
            sb.append(alternative.string());
        }

        return sb.toString();
    }

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }
}
