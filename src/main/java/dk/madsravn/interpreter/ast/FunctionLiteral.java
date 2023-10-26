package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionLiteral  implements IExpression{
    private Token token;
    private List<Identifier> parameters;
    private BlockStatement body;

    public FunctionLiteral(Token token, List<Identifier> parameters, BlockStatement body) {
        this.token = token;
        this.parameters = parameters;
        this.body = body;
    }

    public List<Identifier> getParameters() {
        return parameters;
    }

    public BlockStatement getBody() {
        return body;
    }

    public int getParametersLength() {
        return parameters.size();
    }
    @Override
    public void expressionNode() {}

    @Override
    public String string() {
        StringBuilder sb = new StringBuilder();
        String paramString = parameters.stream().map(p -> p.string()).collect(Collectors.joining(","));

        sb.append(token.getLiteral());
        sb.append("(");
        sb.append(paramString);
        sb.append(")");
        sb.append(body.string());

        return sb.toString();
    }

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }
}
