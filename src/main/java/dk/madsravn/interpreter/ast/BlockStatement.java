package dk.madsravn.interpreter.ast;

import dk.madsravn.interpreter.tokens.Token;

import java.util.List;
import java.util.stream.Collectors;

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
        String statementString = statements.stream().map(statement -> statement.string()).collect(Collectors.joining(""));

        return statementString;
    }

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }
}
