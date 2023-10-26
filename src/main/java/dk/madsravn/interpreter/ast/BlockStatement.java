package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;

import java.util.List;

public class BlockStatement implements IStatement {
    private Token token;
    private List<IStatement> statements;

    public BlockStatement(Token token, List<IStatement> statements) {
        this.token = token;
        this.statements = statements;
    }

    public int getStatementsLength() {
        return statements.size();
    }

    public List<IStatement> getStatements() {
        return statements;
    }

    @Override
    public void statementNode() {}

    @Override
    public String string() {
        StringBuilder sb = new StringBuilder();
        for(IStatement statement : statements) {
            sb.append(statement.string());
        }

        return sb.toString();
    }

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }
}
