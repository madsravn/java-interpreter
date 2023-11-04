package dk.madsravn.interpreter.evaluator;

import dk.madsravn.interpreter.ast.*;
import dk.madsravn.interpreter.object.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Evaluator {

    private static BooleanObject TRUE = new BooleanObject(true);
    private static BooleanObject FALSE = new BooleanObject(false);
    private static NullObject NULL = new NullObject();

    public static IObject evaluate(INode node, Environment env) {
        // TODO: Ugly?
        if(node instanceof PrefixExpression) {
            PrefixExpression prefixExpression = (PrefixExpression) node;
            IObject right = evaluate(prefixExpression.getRight(), env);
            if(isError(right)) {
                return right;
            }
            return evaluatePrefixExpression(prefixExpression.getOperator(), right);
        }
        if(node instanceof LetStatement) {
            LetStatement letStatement = (LetStatement) node;
            var value = evaluate(letStatement.getValue(), env);
            if(isError(value)) {
                return value;
            }
            env.set(letStatement.getName().getValue(), value);
        }
        if(node instanceof Identifier) {
            return evaluateIdentifier(node, env);
        }
        if(node instanceof CallExpression) {
            CallExpression callExpression = (CallExpression) node;
            var function = evaluate(callExpression.getFunction(), env);
            if(isError(function)) {
                return function;
            }
            var args = evaluateExpressions(callExpression.getArguments(), env);
            if(args.size() == 1 && isError(args.get(0))) {
                return args.get(0);
            }

            return applyFunction(function, args);

        }
        if(node instanceof InfixExpression) {
            InfixExpression infixExpression = (InfixExpression) node;
            IObject left = evaluate(infixExpression.getLeft(), env);
            if(isError(left)) {
                return left;
            }
            IObject right = evaluate(infixExpression.getRight(), env);
            if(isError(right)) {
                return right;
            }
            return evaluateInfixExpression(infixExpression.getOperator(), left, right);
        }
        if(node instanceof FunctionLiteral) {
            FunctionLiteral functionLiteral = (FunctionLiteral) node;
            return new FunctionObject(functionLiteral.getParameters(), functionLiteral.getBody(), env);
        }
        if(node instanceof ReturnStatement) {
            ReturnStatement returnStatement = (ReturnStatement) node;
            IObject value = evaluate(returnStatement.getExpression(), env);
            if(isError(value)) {
                return value;
            }
            return new ReturnObject(value);
        }
        if(node instanceof BlockStatement) {
            return evaluateBlockStatement(((BlockStatement)node).getStatements(), env);
        }
        if(node instanceof IfExpression) {
            return evaluateIfExpression(node, env);
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
            return evaluate(((ExpressionStatement)node).getExpression(), env);
        }
        if(node instanceof Program) {
            return evaluateProgram(((Program)node).getStatements(), env);
        }
        return null;
    }

    private static IObject evaluateIfExpression(INode node, Environment env) {
        IfExpression ifExpression = (IfExpression) node;
        var condition = evaluate(ifExpression.getCondition(), env);
        if(isError(condition)) {
            return condition;
        }
        if(isTruthy(condition)) {
            return evaluate(ifExpression.getConsequence(), env);
        } else if (ifExpression.getAlternative() != null) {
            return evaluate(ifExpression.getAlternative(), env);
        } else {
            return NULL;
        }
    }

    private static boolean isTruthy(IObject object) {
        if (object == NULL) {
            return false;
        } else if (object == TRUE) {
            return true;
        } else if (object == FALSE) {
            return false;
        }
        return true;
    }

    private static IObject evaluateProgram(List<IStatement> statements, Environment env) {
        IObject object = NULL;
        for(IStatement statement : statements) {
            object =  evaluate(statement, env);
            if(object instanceof ReturnObject) {
                ReturnObject returnObject = (ReturnObject) object;
                return returnObject.getValue();
            }
            if(object instanceof ErrorObject) {
                return object;
            }
        }
        return object;
    }

    private static IObject evaluateBlockStatement(List<IStatement> statements, Environment env) {
        IObject object = NULL;
        for(IStatement statement : statements) {
            object =  evaluate(statement, env);
            if(object instanceof ReturnObject || object instanceof ErrorObject) {
                return object;
            }
        }
        return object;
    }

    private static IObject evaluateInfixExpression(String operator, IObject left, IObject right) {
        if(!left.type().equals(right.type())) {
            return ErrorObject.typeMismatchError("" + left.type() + " " + operator + " " + right.type());
        }
        if(left instanceof IntegerObject && right instanceof IntegerObject) {
            return evaluateIntegerInfixExpression(operator, left, right);
        }
        if(left instanceof BooleanObject && right instanceof BooleanObject) {
            var leftValue = ((BooleanObject)left).getValue();
            var rightValue = ((BooleanObject)right).getValue();
            if (operator.equals("==")) {
                return nativeBoolToBooleanObject(leftValue == rightValue);
            }
            if(operator.equals("!=")) {
                return nativeBoolToBooleanObject(leftValue != rightValue);
            }
        }
        return ErrorObject.unknownOperatorError("" + left.type() + " " + operator + " " + right.type());
    }

    private static IObject evaluateIntegerInfixExpression(String operator, IObject left, IObject right) {
        var leftValue = ((IntegerObject)left).getValue();
        var rightValue = ((IntegerObject)right).getValue();
        switch(operator) {
            case "+":
                return new IntegerObject(leftValue + rightValue);
            case "-":
                return new IntegerObject(leftValue - rightValue);
            case "*":
                return new IntegerObject(leftValue * rightValue);
            case "/":
                return new IntegerObject(leftValue / rightValue);
            case "<":
                return nativeBoolToBooleanObject(leftValue < rightValue);
            case ">":
                return nativeBoolToBooleanObject(leftValue > rightValue);
            case "==":
                return nativeBoolToBooleanObject(leftValue == rightValue);
            case "!=":
                return nativeBoolToBooleanObject(leftValue != rightValue);
            default:
                return ErrorObject.unknownOperatorError(left.type() + " " + operator + " " + right.type());
        }
    }

    private static IObject evaluatePrefixExpression(String operator, IObject right) {
        if(operator.equals("!")) {
            return evalulateBangOperatorExpression(right);
        }
        if(operator.equals("-")) {
            return evaluateMinusPrefixOperatorExpression(right);
        }
        return ErrorObject.unknownOperatorError("" + operator + right.type());
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

    private static IObject evaluateMinusPrefixOperatorExpression(IObject right) {
        if(right.type().equals("INTEGER")) {
            if(right instanceof IntegerObject) {
                var value = ((IntegerObject)right).getValue();
                return new IntegerObject(-value);
            }
        }
        return ErrorObject.unknownOperatorError("-" + right.type());
    }

    private static BooleanObject nativeBoolToBooleanObject(boolean input) {
        if(input) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    private static IObject evaluateIdentifier(INode node, Environment env) {
        Identifier identifier = (Identifier) node;

        var value = env.get(identifier.getValue());
        if(value.isPresent()) {
            return value.get();
        } else {
            return ErrorObject.identifierNotFoundError(identifier.getValue());
        }
    }

    private static List<IObject> evaluateExpressions(List<IExpression> expressions, Environment env) {
        List<IObject> result = new ArrayList<IObject>();
        for(IExpression expression : expressions) {
            var evaluated = evaluate(expression, env);
            if(isError(evaluated)) {
                return Arrays.asList(evaluated);
            }
            result.add(evaluated);
        }
        return result;
    }

    private static boolean isError(IObject object) {
        return (object instanceof ErrorObject);
    }
}
