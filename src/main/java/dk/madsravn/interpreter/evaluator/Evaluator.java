package dk.madsravn.interpreter.evaluator;

import dk.madsravn.interpreter.ast.*;
import dk.madsravn.interpreter.object.*;

import java.util.*;

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
        if(node instanceof HashLiteral) {
            HashLiteral hashLiteral = (HashLiteral) node;
            return evaluateHashLiteral(hashLiteral, env);
        }
        if(node instanceof StringLiteral) {
            StringLiteral stringLiteral = (StringLiteral) node;
            return new StringObject(stringLiteral.getValue());
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
        if(node instanceof ArrayLiteral) {
            ArrayLiteral arrayLiteral = (ArrayLiteral) node;
            var elements = evaluateExpressions(arrayLiteral.getElements(), env);
            if(elements.size() == 1 && isError(elements.get(0))) {
                return elements.get(0);
            }
            return new ArrayObject(elements);
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
        if(node instanceof IndexExpression) {
            IndexExpression indexExpression = (IndexExpression) node;
            var left = evaluate(indexExpression.getLeft(), env);
            if(isError(left)) {
                return left;
            }
            var index = evaluate(indexExpression.getIndex(), env);
            if(isError(index)) {
                return index;
            }
            return evaluateIndexExpression(left, index);
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
            return evaluateArrayIndexExpression(left, index);
        }
        if(left instanceof HashObject) {
            return evaluateHashExpression(left, index);
        }
        return ErrorObject.indexOperatorNotSupported(left.type());
    }

    private static IObject evaluateHashExpression(IObject left, IObject index) {
        // TODO:  Is this a stupid thing to do here without a check? Perform before calling or add an `else` branch returning an error object
        HashObject hashObject = (HashObject) left;
        if(index instanceof StringObject || index instanceof BooleanObject || index instanceof IntegerObject) {
            var value =  hashObject.getPairs().get(index);
            if(value != null) {
                return value;
            } else {
                return NULL;
            }
        } else {
            return ErrorObject.unusableAsHashKey(index.type());
        }
    }

    private static IObject evaluateArrayIndexExpression(IObject array, IObject index) {
        ArrayObject arrayObject = (ArrayObject) array;
        var indexValue = ((IntegerObject)index).getValue();
        var max = arrayObject.getElementsLength() - 1;
        if(indexValue < 0 || indexValue > max) {
            return NULL;
        }
        return arrayObject.getElements().get(indexValue);
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
        if(left instanceof StringObject && right instanceof StringObject) {
            var leftValue = (StringObject)left;
            var rightValue = (StringObject)right;
            return evaluateStringInfixExpression(operator, leftValue, rightValue);
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

    private static IObject evaluateStringInfixExpression(String operator, StringObject left, StringObject right) {
        if(!operator.equals("+")) {
            return ErrorObject.unknownOperatorError(left.type() + " " + operator + " " + right.type());
        }
        return new StringObject(left.getValue() + right.getValue());
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
        if(objects.get(0) instanceof ArrayObject) {
            ArrayObject arrayObject = (ArrayObject) objects.get(0);
            List<IObject> newElements = new ArrayList<IObject>();
            newElements.addAll(arrayObject.getElements());
            newElements.add(objects.get(1));
            return new ArrayObject(newElements);
        }

        return ErrorObject.argumentToFirstMustBeArray(objects.get(0).type());
    }
    public static IObject firstOfArray(List<IObject> objects) {
        if(objects.size() != 1) {
            return ErrorObject.wrongNumberOfArguments(1, objects.size());
        }

        if(objects.get(0) instanceof ArrayObject) {
            ArrayObject arrayObject = (ArrayObject) objects.get(0);
            if(arrayObject.getElementsLength() > 0) {
                return arrayObject.getElements().get(0); // TODO: Update JAVA version to get getFirst
            } else {
                return NULL;
            }
        }
        return ErrorObject.argumentToFirstMustBeArray(objects.get(0).type());

    }

    public static IObject restOfArray(List<IObject> objects) {
        if(objects.size() != 1) {
            return ErrorObject.wrongNumberOfArguments(1, objects.size());
        }

        if(objects.get(0) instanceof ArrayObject) {
            ArrayObject arrayObject = (ArrayObject) objects.get(0);
            if(arrayObject.getElementsLength() == 0) {
                return NULL;
            } else if(arrayObject.getElementsLength() == 1) {
                List<IObject> elements = new ArrayList<IObject>();
                return new ArrayObject(elements);

            } else if(arrayObject.getElementsLength() > 1) {
                List<IObject> elements = arrayObject.getElements().subList(1, arrayObject.getElementsLength());
                return new ArrayObject(elements);
            }
        }

        return ErrorObject.argumentToFirstMustBeArray(objects.get(0).type());
    }

    public static IObject lastOfArray(List<IObject> objects) {
        if(objects.size() != 1) {
            return ErrorObject.wrongNumberOfArguments(1, objects.size());
        }

        if(objects.get(0) instanceof ArrayObject) {
            ArrayObject arrayObject = (ArrayObject) objects.get(0);
            if(arrayObject.getElementsLength() > 0) {
                return arrayObject.getElements().get(arrayObject.getElementsLength() - 1); //TODO: Update JAVA to get getLast()
            } else {
                return NULL;
            }
        }
        return ErrorObject.argumentToFirstMustBeArray(objects.get(0).type());

    }

    public static IObject lengthOfObject(List<IObject> objects) {
        if(objects.size() != 1) {
            return ErrorObject.wrongNumberOfArguments(1, objects.size());
        }
        if(objects.get(0) instanceof StringObject) {
            var value = ((StringObject)objects.get(0)).getValue();
            return new IntegerObject(value.length());
        }
        if(objects.get(0) instanceof ArrayObject) {
            var length = ((ArrayObject)objects.get(0)).getElementsLength();
            return new IntegerObject(length);
        }
        return ErrorObject.argumentNotSupported("len", objects.get(0).type());
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
