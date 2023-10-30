package dk.madsravn.interpreter.evaluator;

import dk.madsravn.interpreter.ast.*;
import dk.madsravn.interpreter.object.IObject;
import dk.madsravn.interpreter.object.IntegerObject;

import java.util.List;

public class Evaluator {

    public static IObject evaluate(INode node) {
        // TODO: Ugly?
        if(node instanceof IntegerLiteral) {
            // TODO: Ugly?
            return new IntegerObject(((IntegerLiteral) node).getValue());
        }
        if(node instanceof ExpressionStatement) {
            return evaluate(((ExpressionStatement)node).getExpression());
        }
        if(node instanceof Program) {
            return evaluateStatements(((Program)node).getStatements());
        }
        return null;
    }

    private static IObject evaluateStatements(List<IStatement> statements) {
        for(IStatement statement : statements) {
            return evaluate(statement);
        }
        return null;
    }
}
