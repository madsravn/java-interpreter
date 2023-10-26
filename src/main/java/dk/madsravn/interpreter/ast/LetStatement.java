package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;
import dk.madsravn.interpreter.tokens.TokenType;

public class LetStatement implements IStatement {
    Token token;
    Identifier name;
    IExpression value;

    public LetStatement(Token token, Identifier name, IExpression value) {
        this.token = token;
        this.name = name;
        this.value = value;
    }
    @Override
    public void statementNode() {}

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    public void setName(Identifier identifier) {
        this.name = identifier;
    }

    public Identifier getName() {
        return this.name;
    }

    @Override
    public String string() {
        StringBuilder sb = new StringBuilder();
        sb.append(tokenLiteral() + " ");
        sb.append(name.string());
        sb.append(" = ");
        if(value != null) {
            sb.append(value.string());
        }
        sb.append(";");

        return sb.toString();
    }
}
