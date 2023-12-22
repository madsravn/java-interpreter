package dk.madsravn.interpreter.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Program implements INode{
    List<IStatement> statements;

    public Program() {
        statements = new ArrayList<IStatement>();
    }

    public void addStatement(IStatement statement) {
        statements.add(statement);
    }

    public int getStatementsLength() {
        return statements.size();
    }

    public List<IStatement> getStatements() {
        return statements;
    }

    @Override
    public String tokenLiteral() {
        if (statements.size() > 0) {
            return statements.get(0).tokenLiteral();
        } else {
            return "";
        }
    }

    @Override
    public String string() {
        return statements.stream().map(s -> s.string()).collect(Collectors.joining(""));
    }
}
