package dk.madsravn.interpreter.evaluator;

import dk.madsravn.interpreter.ast.*;
import dk.madsravn.interpreter.object.BooleanObject;
import dk.madsravn.interpreter.object.IObject;
import dk.madsravn.interpreter.object.IntegerObject;
import dk.madsravn.interpreter.object.NullObject;

import java.util.List;

public class Evaluator {

    private static BooleanObject TRUE = new BooleanObject(true);
    private static BooleanObject FALSE = new BooleanObject(false);
    private static NullObject NULL = new NullObject();

    public static IObject evaluate(INode node) {
        // TODO: Ugly?
        if(node instanceof PrefixExpression) {
            PrefixExpression prefixExpression = (PrefixExpression) node;
            IObject right = evaluate(prefixExpression.getRight());
            return evaluatePrefixExpression(prefixExpression.getOperator(), right);
        }
        if(node instanceof IntegerLiteral) {
            return new IntegerObject(((IntegerLiteral) node).getValue());
        }
        if(node instanceof BooleanType) {
            boolean value =  ((BooleanType)node).getValue();
            if (value == true) {
                return TRUE;
            } else {
                return FALSE;
            }
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
        return NULL;
    }

    private static IObject evaluatePrefixExpression(String operator, IObject right) {
        if(operator.equals("!")) {
            return evalulateBangOperatorExpression(right);
        }
        return new NullObject();
    }

    private static IObject evalulateBangOperatorExpression(IObject right) {
        if (right.equals(TRUE)) {
            return FALSE;
        } else if (right.equals(FALSE) || right.equals(NULL)) {
            return TRUE;
        }
        if(right instanceof IntegerObject) {
            IntegerObject integerObject = (IntegerObject) right;
            if (integerObject.getValue() == 0) {
                return TRUE;
            } else {
                return FALSE;
            }
        }
        return FALSE;

    }
}
