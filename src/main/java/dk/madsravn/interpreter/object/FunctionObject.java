package dk.madsravn.interpreter.object;

import dk.madsravn.interpreter.ast.BlockStatement;
import dk.madsravn.interpreter.ast.Identifier;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionObject implements IObject {
    private static String OBJ_TYPE = "FUNCTION";
    private List<Identifier> parameters;
    private BlockStatement body;
    private Environment env;
    public FunctionObject(List<Identifier> parameters, BlockStatement body, Environment env) {
        this.parameters = parameters;
        this.body = body;
        this.env = env;
    }

    public int getParametersLength() {
        return parameters.size();
    }

    public List<Identifier> getParameters() {
        return parameters;
    }

    public BlockStatement getBody() {
        return body;
    }

    @Override
    public String type() {
        return OBJ_TYPE;
    }

    @Override
    public String inspect() {
        StringBuilder sb = new StringBuilder();
        String paramString = parameters.stream().map(p -> p.string()).collect(Collectors.joining(", "));
        sb.append("fn");
        sb.append("(");
        sb.append(paramString);
        sb.append(") {\n");
        sb.append(body.string());
        sb.append("\n");

        return sb.toString();
    }
}
