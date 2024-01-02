package dk.madsravn.interpreter.evaluator;

import dk.madsravn.interpreter.ast.*;
import dk.madsravn.interpreter.object.*;

import java.util.*;

public class Evaluator {

    private static BooleanObject TRUE = new BooleanObject(true);
    private static BooleanObject FALSE = new BooleanObject(false);
    private static NullObject NULL = new NullObject();

    public static IObject evaluate(INode node, Environment env) {
        return switch(node) {
            case PrefixExpression prefixExpression -> {
                IObject right = evaluate(prefixExpression.getRight(), env);
                if(isError(right)) {
                    yield right;
                }
                yield evaluatePrefixExpression(prefixExpression.getOperator(), right);
            }

            case LetStatement letStatement -> {
                var value = evaluate(letStatement.getValue(), env);
                if(isError(value)) {
                    yield value;
                }
                env.set(letStatement.getName().getValue(), value);
                yield null;
            }

            case Identifier identifier -> evaluateIdentifier(identifier, env);

            case HashLiteral hashLiteral -> evaluateHashLiteral(hashLiteral, env);

            case StringLiteral stringLiteral -> new StringObject(stringLiteral.getValue());

            case CallExpression callExpression -> {
                var function = evaluate(callExpression.getFunction(), env);
                if(isError(function)) {
                    yield function;
                }
                var args = evaluateExpressions(callExpression.getArguments(), env);
                if(args.size() == 1 && isError(args.get(0))) {
                    yield args.get(0);
                }

                yield applyFunction(function, args);
            }

            case ArrayLiteral arrayLiteral -> {
                var elements = evaluateExpressions(arrayLiteral.getElements(), env);
                if(elements.size() == 1 && isError(elements.get(0))) {
                    yield elements.get(0);
                }
                yield new ArrayObject(elements);
            }

            case InfixExpression infixExpression -> {
                IObject left = evaluate(infixExpression.getLeft(), env);
                if(isError(left)) {
                    yield left;
                }
                IObject right = evaluate(infixExpression.getRight(), env);
                if(isError(right)) {
                    yield right;
                }
                yield evaluateInfixExpression(infixExpression.getOperator(), left, right);
            }

            case FunctionLiteral functionLiteral ->
                    new FunctionObject(functionLiteral.getParameters(), functionLiteral.getBody(), env);

            case ReturnStatement returnStatement -> {
                IObject value = evaluate(returnStatement.getExpression(), env);
                if(isError(value)) {
                    yield value;
                }
                yield new ReturnObject(value);
            }

            case IndexExpression indexExpression -> {
                var left = evaluate(indexExpression.getLeft(), env);
                if(isError(left)) {
                    yield left;
                }
                var index = evaluate(indexExpression.getIndex(), env);
                if(isError(index)) {
                    yield index;
                }
                yield evaluateIndexExpression(left, index);
            }

            case BlockStatement blockStatement -> evaluateBlockStatement(blockStatement.getStatements(), env);

            case IfExpression ifExpression -> evaluateIfExpression(ifExpression, env);

            case IntegerLiteral integerLiteral -> new IntegerObject(integerLiteral.getValue());

            case BooleanType booleanType -> {
                boolean value =  booleanType.getValue();
                if (value == true) {
                    yield TRUE;
                } else {
                    yield FALSE;
                }
            }

            case ExpressionStatement expressionStatement -> evaluate(expressionStatement.getExpression(), env);

            case Program program -> evaluateProgram(program.getStatements(), env);

            //TODO: What is missing? Interfaces?
            default -> null;
        };
    }

    private static IObject evaluateHashLiteral(HashLiteral hashLiteral, Environment env) {
        Map<IObject, IObject> pairs = new HashMap<>();
        for(Map.Entry<IExpression, IExpression> entry : hashLiteral.getPairs().entrySet()) {
            var key = evaluate(entry.getKey(), env);
            if(isError(key)) {
                return key;
            }
            var value = evaluate(entry.getValue(), env);
            if(isError(value)) {
                return value;
            }
            pairs.put(key, value);
        }

        return new HashObject(pairs);
    }

    private static IObject evaluateIndexExpression(IObject left, IObject index) {
        if(left instanceof ArrayObject && index instanceof IntegerObject) {
            return evaluateArrayIndexExpression((ArrayObject) left, index);
        }
        if(left instanceof HashObject) {
            return evaluateHashExpression((HashObject) left, index);
        }

        return ErrorObject.indexOperatorNotSupported(left.type());
    }

    private static IObject evaluateHashExpression(HashObject left, IObject index) {
        if(index instanceof StringObject || index instanceof BooleanObject || index instanceof IntegerObject) {
            var value =  left.getPairs().get(index);
            if(value != null) {
                return value;
            } else {
                return NULL;
            }
        } else {
            return ErrorObject.unusableAsHashKey(index.type());
        }
    }

    private static IObject evaluateArrayIndexExpression(ArrayObject array, IObject index) {
        var indexValue = ((IntegerObject)index).getValue();
        var max = array.getElementsLength() - 1;
        if(indexValue < 0 || indexValue > max) {
            return NULL;
        }

        return array.getElements().get(indexValue);
    }

    private static IObject evaluateIfExpression(IfExpression ifExpression, Environment env) {
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
        if(left instanceof StringObject && right instanceof StringObject) {
            return evaluateStringInfixExpression(operator, (StringObject) left, (StringObject) right);
        }
        if(left instanceof IntegerObject && right instanceof IntegerObject) {
            return evaluateIntegerInfixExpression(operator, (IntegerObject) left, (IntegerObject) right);
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

    private static IObject evaluateStringInfixExpression(String operator, StringObject left, StringObject right) {
        if(!operator.equals("+")) {
            return ErrorObject.unknownOperatorError(left.type() + " " + operator + " " + right.type());
        }

        return new StringObject(left.getValue() + right.getValue());
    }

    private static IObject evaluateIntegerInfixExpression(String operator, IntegerObject left, IntegerObject right) {
        var leftValue = left.getValue();
        var rightValue = right.getValue();
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
            return evaluateBangOperatorExpression(right);
        }
        if(operator.equals("-")) {
            return evaluateMinusPrefixOperatorExpression(right);
        }

        return ErrorObject.unknownOperatorError("" + operator + right.type());
    }

    private static IObject evaluateBangOperatorExpression(IObject right) {
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
        }

        var builtin = findBuiltinFunction(identifier.getValue());
        if(builtin.isPresent()) {
            return builtin.get();
        }

        return ErrorObject.identifierNotFoundError(identifier.getValue());
    }

    private static Optional<IObject> findBuiltinFunction(String name) {
        if(name.equals("len")) {
            return Optional.of(new BuiltinFunctionObject(Evaluator::lengthOfObject));
            //return Optional.of(new BuiltinFunctionObject((a) -> NULL));
        }
        if(name.equals("first")) {
            return Optional.of(new BuiltinFunctionObject(Evaluator::firstOfArray));
        }
        if(name.equals("last")) {
            return Optional.of(new BuiltinFunctionObject(Evaluator::lastOfArray));
        }
        if(name.equals("rest")) {
            return Optional.of(new BuiltinFunctionObject(Evaluator::restOfArray));
        }
        if(name.equals("push")) {
            return Optional.of(new BuiltinFunctionObject(Evaluator::pushArray));
        }
        if(name.equals("puts")) {
            return Optional.of(new BuiltinFunctionObject(Evaluator::printLine));
        }
        return Optional.empty();
    }

    //TODO: first, last and rest of arrays needs to return something if they are ArrayObject.
    // Only return an error if type is wrong.

    // TODO: MOVES THESE PUBLIC STATICS TO OWN FILE

    public static IObject printLine(List<IObject> objects) {
        objects.stream().map(e -> e.inspect()).forEach(System.out::println);
        return NULL;
    }

    public static IObject pushArray(List<IObject> objects) {
        if(objects.size() != 2) {
            return ErrorObject.wrongNumberOfArguments(2, objects.size());
        }
        if(objects.getFirst() instanceof ArrayObject) {
            ArrayObject arrayObject = (ArrayObject) objects.get(0);
            List<IObject> newElements = new ArrayList<>();
            newElements.addAll(arrayObject.getElements());
            newElements.add(objects.getLast());
            return new ArrayObject(newElements);
        }

        return ErrorObject.argumentToFirstMustBeArray(objects.getFirst().type());
    }
    public static IObject firstOfArray(List<IObject> objects) {
        if(objects.size() != 1) {
            return ErrorObject.wrongNumberOfArguments(1, objects.size());
        }

        if(objects.get(0) instanceof ArrayObject) {
            ArrayObject arrayObject = (ArrayObject) objects.getFirst();
            if(arrayObject.getElementsLength() > 0) {
                return arrayObject.getElements().getFirst();
            } else {
                return NULL;
            }
        }

        return ErrorObject.argumentToFirstMustBeArray(objects.getFirst().type());
    }

    public static IObject restOfArray(List<IObject> objects) {
        if(objects.size() != 1) {
            return ErrorObject.wrongNumberOfArguments(1, objects.size());
        }

        if(objects.get(0) instanceof ArrayObject) {
            ArrayObject arrayObject = (ArrayObject) objects.getFirst();
            if(arrayObject.getElementsLength() == 0) {
                return NULL;
            } else if(arrayObject.getElementsLength() == 1) {
                List<IObject> elements = new ArrayList<>();
                return new ArrayObject(elements);

            } else if(arrayObject.getElementsLength() > 1) {
                List<IObject> elements = arrayObject.getElements().subList(1, arrayObject.getElementsLength());
                return new ArrayObject(elements);
            }
        }

        return ErrorObject.argumentToFirstMustBeArray(objects.getFirst().type());
    }

    public static IObject lastOfArray(List<IObject> objects) {
        if(objects.size() != 1) {
            return ErrorObject.wrongNumberOfArguments(1, objects.size());
        }

        if(objects.get(0) instanceof ArrayObject) {
            ArrayObject arrayObject = (ArrayObject) objects.getFirst();
            if(arrayObject.getElementsLength() > 0) {
                return arrayObject.getElements().getLast();
            } else {
                return NULL;
            }
        }

        return ErrorObject.argumentToFirstMustBeArray(objects.getFirst().type());

    }

    public static IObject lengthOfObject(List<IObject> objects) {
        if(objects.size() != 1) {
            return ErrorObject.wrongNumberOfArguments(1, objects.size());
        }
        if(objects.getFirst() instanceof StringObject) {
            var value = ((StringObject)objects.getFirst()).getValue();
            return new IntegerObject(value.length());
        }
        if(objects.getFirst() instanceof ArrayObject) {
            var length = ((ArrayObject)objects.getFirst()).getElementsLength();
            return new IntegerObject(length);
        }

        return ErrorObject.argumentNotSupported("len", objects.getFirst().type());
    }

    private static List<IObject> evaluateExpressions(List<IExpression> expressions, Environment env) {
        List<IObject> result = new ArrayList<>();
        for(IExpression expression : expressions) {
            var evaluated = evaluate(expression, env);
            if(isError(evaluated)) {
                return Arrays.asList(evaluated);
            }
            result.add(evaluated);
        }

        return result;
    }

    private static IObject applyFunction(IObject function, List<IObject> arguments) {
        if(function instanceof FunctionObject) {
            FunctionObject functionObject = (FunctionObject) function;
            Environment extendedEnvironment = extendFunctionEnvironment(functionObject, arguments);
            IObject evaluated = evaluate(functionObject.getBody(), extendedEnvironment);
            return unwrapReturnValue(evaluated);
        }
        if(function instanceof BuiltinFunctionObject) {
            BuiltinFunctionObject builtinFunctionObject = (BuiltinFunctionObject) function;
            return builtinFunctionObject.apply(arguments);
        }

        return ErrorObject.notAFunction(function.type());
    }

    private static Environment extendFunctionEnvironment(FunctionObject function, List<IObject> arguments) {
        Environment env = new Environment(function.getEnvironment());
        for(int i = 0; i < function.getParametersLength(); ++i) {
            env.set(function.getParameters().get(i).getValue(), arguments.get(i));
        }

        return env;
    }

    private static IObject unwrapReturnValue(IObject object) {
        if(object instanceof ReturnObject) {
            ReturnObject returnObject = (ReturnObject) object;
            return returnObject.getValue();
        }

        return object;
    }

    private static boolean isError(IObject object) {
        return (object instanceof ErrorObject);
    }
}
