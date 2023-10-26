package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;

import java.util.List;
import java.util.stream.Collectors;

public class CallExpression implements IExpression {
    private Token token;
    private IExpression function;
    private List<IExpression> arguments;

    public CallExpression(Token token, IExpression function, List<IExpression> arguments) {
        this.token = token;
        this.function = function;
        this.arguments = arguments;
    }

    public IExpression getFunction() {
        return function;
    }

    public List<IExpression> getArguments() {
        return arguments;
    }
    @Override
    public void expressionNode() {}

    public String string() {
        StringBuilder sb = new StringBuilder();
        String argumentString = arguments.stream().map(p -> p.string()).collect(Collectors.joining(","));
        sb.append(function.string());
        sb.append("(");
        sb.append(argumentString);
        sb.append(")");

        return sb.toString();
    }

    public String tokenLiteral() {
        return token.getLiteral();
    }
}
