package dk.madsravn.interpreter.evaluator;

import dk.madsravn.interpreter.ast.Program;
import dk.madsravn.interpreter.lexer.Lexer;
import dk.madsravn.interpreter.object.*;
import dk.madsravn.interpreter.parser.Parser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static dk.madsravn.interpreter.evaluator.Evaluator.evaluate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EvaluatorTest {

    private record EvalIntegerData(String input, Integer value) { }
    private record EvalBooleanData(String input, boolean value) { }
    private record EvalStringData(String input, String value) { }

    @Test
    public void testEvalIntegerExpression() {
        List<EvalIntegerData> inputs = Arrays.asList(
                new EvalIntegerData("5", 5),
                new EvalIntegerData("10", 10),
                new EvalIntegerData("-5", -5),
                new EvalIntegerData("-10", -10),
                new EvalIntegerData("5 + 5 + 5 + 5 - 10", 10),
                new EvalIntegerData("2 * 2 * 2 * 2 * 2", 32),
                new EvalIntegerData("-50 + 100 - 50", 0),
                new EvalIntegerData("5 * 2 + 10", 20),
                new EvalIntegerData("5 + 2 * 10", 25),
                new EvalIntegerData("20 + 2 * -10", 0),
                new EvalIntegerData("50 / 2 * 2 + 10", 60),
                new EvalIntegerData("2 * (5 + 10)", 30),
                new EvalIntegerData("3 * 3 * 3 + 10", 37),
                new EvalIntegerData("3 * (3 * 3) + 10", 37),
                new EvalIntegerData("(5 + 10 * 2 + 15 / 3) * 2 + -10", 50)
        );

        for(EvalIntegerData input : inputs) {
            IObject object = testEval(input.input);
            testIntegerObject(object, input.value);
        }
    }

    @Test
    public void testBooleanExpression() {
        List<EvalBooleanData> inputs = Arrays.asList(
                new EvalBooleanData("true", true),
                new EvalBooleanData("false", false),
                new EvalBooleanData("1 < 2", true),
                new EvalBooleanData("1 > 2", false),
                new EvalBooleanData("1 < 1", false),
                new EvalBooleanData("1 > 1", false),
                new EvalBooleanData("1 == 1", true),
                new EvalBooleanData("1 != 1", false),
                new EvalBooleanData("1 == 2", false),
                new EvalBooleanData("1 != 2", true),
                new EvalBooleanData("true == true", true),
                new EvalBooleanData("false == false", true),
                new EvalBooleanData("false == true", false),
                new EvalBooleanData("true == false", false),
                new EvalBooleanData("true != true", false),
                new EvalBooleanData("false != false", false),
                new EvalBooleanData("(1 < 2) == true", true),
                new EvalBooleanData("(1 < 2) == false", false),
                new EvalBooleanData("(1 > 2) == true", false),
                new EvalBooleanData("(1 > 2) == false", true)
        );

        for(EvalBooleanData input : inputs) {
            IObject object = testEval(input.input);
            testBooleanObject(object, input.value, input.input);
        }
    }

    @Test
    public void testBangOperator() {
        List<EvalBooleanData> inputs = Arrays.asList(
                new EvalBooleanData("!true", false),
                new EvalBooleanData("!false", true),
                new EvalBooleanData("!5", false),
                new EvalBooleanData("!!true", true),
                new EvalBooleanData("!!false", false),
                new EvalBooleanData("!!5", true),
                new EvalBooleanData("!!0", false),
                new EvalBooleanData("!0", true)
        );
        for(EvalBooleanData input : inputs) {
            IObject object = testEval(input.input);
            testBooleanObject(object, input.value, input.input);
        }
    }

    @Test
    public void testIfElseExpression() {
        List<EvalIntegerData> inputs = Arrays.asList(
                new EvalIntegerData("if (true) { 10 }", 10),
                new EvalIntegerData("if (false) { 10 }", null),
                new EvalIntegerData("if (1) { 10 }", 10),
                new EvalIntegerData("if (1 < 2) { 10 }", 10),
                new EvalIntegerData("if (1 > 2) { 10 }", null),
                new EvalIntegerData("if (1 > 2) { 10 } else { 20 }", 20),
                new EvalIntegerData("if (1 < 2) { 10 } else { 20 }", 10)
        );
        for(EvalIntegerData input : inputs) {
            IObject object = testEval(input.input);
            if(input.value != null) {
                testIntegerObject(object, input.value);
            } else {
                testNullObject(object);
            }
        }
    }

    @Test
    public void testReturnStatements() {
        List<EvalIntegerData> inputs = Arrays.asList(
                new EvalIntegerData("return 10;", 10),
                new EvalIntegerData("return 10; 9;", 10),
                new EvalIntegerData("return 2 * 5; 9;", 10),
                new EvalIntegerData("9; return 2 * 5; 9;", 10),
                new EvalIntegerData("if (10 > 1) { if (10 > 1) { return 10; } return 1; }", 10)
        );

        for(EvalIntegerData input : inputs) {
            IObject object = testEval(input.input);
            testIntegerObject(object, input.value);
        }
    }

    @Test
    public void testErrorHandling() {
        List<EvalStringData> inputs = Arrays.asList(
                new EvalStringData("5 + true;", "type mismatch: INTEGER + BOOLEAN"),
                new EvalStringData("5 + true; 5;", "type mismatch: INTEGER + BOOLEAN"),
                new EvalStringData("-true;", "unknown operator: -BOOLEAN"),
                new EvalStringData("false + true;", "unknown operator: BOOLEAN + BOOLEAN"),
                new EvalStringData("5; false + true; 5;", "unknown operator: BOOLEAN + BOOLEAN"),
                new EvalStringData("if (10 > 1) { true + false; }", "unknown operator: BOOLEAN + BOOLEAN"),
                new EvalStringData("foobar", "identifier not found: foobar")
        );

        for(EvalStringData input : inputs) {
            IObject object = testEval(input.input);
            testErrorObject(object, input.value);
        }
    }

    @Test
    public void testLetStatements() {
        List<EvalIntegerData> inputs = Arrays.asList(
                new EvalIntegerData("let a = 5; a;", 5),
                new EvalIntegerData("let a = 5 * 5; a;", 25),
                new EvalIntegerData("let a = 5; let b = a; b;", 5),
                new EvalIntegerData("let a = 5; let b = a; let c = a + b + 5; c;", 15)
        );
        for(EvalIntegerData input : inputs) {
            IObject object = testEval(input.input);
            testIntegerObject(object, input.value);
        }
    }

    @Test
    public void testFunctionObject() {
        String input = "fn(x) { x + 2 };";
        IObject object = testEval(input);
        assertTrue(object instanceof FunctionObject);
        FunctionObject functionObject = (FunctionObject) object;
        assertEquals(functionObject.getParametersLength(), 1);

        assertEquals(functionObject.getParameters().get(0).string(), "x");
        assertEquals(functionObject.getBody().string(), "(x + 2)");
    }

    @Test
    public void testFunctionApplication() {
        List<EvalIntegerData> inputs = Arrays.asList(
                new EvalIntegerData("let identity = fn(x) { x; }; identity(5);", 5),
                new EvalIntegerData("let identity = fn(x) { return x; }; identity(5);", 5),
                new EvalIntegerData("let double = fn(x) { 2 * x; }; double(5);", 10),
                new EvalIntegerData("let add = fn(x, y) { x + y; }; add(5, 5);", 10),
                new EvalIntegerData("let add = fn(x, y) { x + y; }; add(5 + 5, add(5, 5));", 20),
                new EvalIntegerData("fn(x) { x; }(5);", 5)
        );

        for(EvalIntegerData input : inputs) {
            IObject object = testEval(input.input);
            testIntegerObject(object, input.value);
        }
    }

    private void testErrorObject(IObject object, String message) {
        assertTrue(object instanceof ErrorObject);
        ErrorObject errorObject = (ErrorObject) object;
        assertEquals(errorObject.getMessage(), message);
    }

    private void testNullObject(IObject object) {
        assertTrue(object instanceof NullObject);
    }

    private void testIntegerObject(IObject object, int expected) {
        assertTrue(object instanceof IntegerObject);
        IntegerObject integerObject = (IntegerObject) object;
        assertEquals(integerObject.getValue(), expected);
    }

    private void testBooleanObject(IObject object, boolean expected, String input) {
        assertTrue(object instanceof BooleanObject);
        BooleanObject booleanObject = (BooleanObject) object;
        assertEquals(booleanObject.getValue(), expected, input);
    }

    private IObject testEval(String input) {
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        Environment env = new Environment();

        return evaluate(program, env);
    }
}
