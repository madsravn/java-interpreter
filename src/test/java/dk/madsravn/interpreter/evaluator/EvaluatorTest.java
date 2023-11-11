package dk.madsravn.interpreter.evaluator;

import dk.madsravn.interpreter.ast.Program;
import dk.madsravn.interpreter.lexer.Lexer;
import dk.madsravn.interpreter.object.*;
import dk.madsravn.interpreter.parser.Parser;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static dk.madsravn.interpreter.evaluator.Evaluator.evaluate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EvaluatorTest {

    private record EvalIntegerData(String input, Integer value) { }
    private record EvalBooleanData(String input, boolean value) { }
    private record EvalStringData(String input, String value) { }
    private record EvalIntegerArrayData(String input, List<Integer> value) { }

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
                new EvalStringData("foobar", "identifier not found: foobar"),
                new EvalStringData("\"Hello\" - \"World\"", "unknown operator: STRING - STRING")
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

    @Test
    public void testStringLiteral() {
        String input = """
                "Hello World!"
                """;
        IObject evaluated = testEval(input);
        assertTrue(evaluated instanceof StringObject);
        StringObject stringObject = (StringObject) evaluated;
        assertEquals(stringObject.getValue(), "Hello World!");
    }

    @Test
    public void testStringConcatenation() {
        String input = """
                "Hello" + " " + "World!"
                """;
        IObject evaluated = testEval(input);
        assertTrue(evaluated instanceof StringObject);
        StringObject stringObject = (StringObject) evaluated;
        assertEquals(stringObject.getValue(), "Hello World!");
    }

    @Test
    public void testBuiltinFunctionsGood() {
        List<EvalIntegerData> inputs = Arrays.asList(
                new EvalIntegerData("len(\"\")", 0),
                new EvalIntegerData("len(\"four\")", 4),
                new EvalIntegerData("len(\"Hello world\")", 11)
        );
        for(EvalIntegerData input : inputs) {
            IObject object = testEval(input.input);
            testIntegerObject(object, input.value);
        }
    }

    @Test
    public void testBuiltinFunctionsBad() {
        List<EvalStringData> inputs = Arrays.asList(
                new EvalStringData("len(1)","argument to `len` not supported, got INTEGER"),
                new EvalStringData("len(\"one\", \"two\")", "wrong number of arguments. got=2, want=1")
        );
        for(EvalStringData input : inputs) {
            IObject object = testEval(input.input);
            testErrorObject(object, input.value);
        }
    }

    @Test
    public void testArrayLiterals() {
        String input = "[1, 2 * 2, 3 + 3]";
        var evaluated = testEval(input);
        assertTrue(evaluated instanceof ArrayObject);
        ArrayObject arrayObject = (ArrayObject) evaluated;
        assertEquals(arrayObject.getElementsLength(), 3);

        testIntegerObject(arrayObject.getElements().get(0), 1);
        testIntegerObject(arrayObject.getElements().get(1), 4);
        testIntegerObject(arrayObject.getElements().get(2), 6);
    }

    @Test
    public void testArrayIndexExpressions() {
        List<EvalIntegerData> inputs = Arrays.asList(
                new EvalIntegerData("[1, 2, 3][0]", 1),
                new EvalIntegerData("[1, 2, 3][1]", 2),
                new EvalIntegerData("[1, 2, 3][2]", 3),
                new EvalIntegerData("let i = 0; [1][i];", 1),
                new EvalIntegerData("[1, 2, 3][1 + 1];", 3),
                new EvalIntegerData("let myArray = [1, 2, 3]; myArray[2];", 3),
                new EvalIntegerData("let myArray = [1, 2, 3]; myArray[0] + myArray[1] + myArray[2];", 6),
                new EvalIntegerData("let myArray = [1, 2, 3]; let i = myArray[0]; myArray[i];", 2),
                new EvalIntegerData("[1, 2, 3][3]", null),
                new EvalIntegerData("[1, 2, 3][-1]", null)
        );

        for(EvalIntegerData input : inputs) {
            var evaluated = testEval(input.input);
            if(input.value != null) {
                testIntegerObject(evaluated, input.value);
            } else {
                testNullObject(evaluated);
            }
        }
    }

    @Test
    public void testArrayBuiltins() {
        List<EvalIntegerArrayData> inputs = Arrays.asList(
                new EvalIntegerArrayData("rest([1, 2, 3]);", Arrays.asList(2, 3)),
                new EvalIntegerArrayData("first([1, 2, 3]);", Arrays.asList(1)),
                new EvalIntegerArrayData("last([1, 2, 3]);", Arrays.asList(3)),
                new EvalIntegerArrayData("rest([1, 2, 3, 4]);", Arrays.asList(2, 3, 4)),
                new EvalIntegerArrayData("first([1, 2, 3, 4]);", Arrays.asList(1)),
                new EvalIntegerArrayData("last([1, 2, 3, 4]);", Arrays.asList(4)),
                new EvalIntegerArrayData("rest([3]);", Arrays.asList()),
                new EvalIntegerArrayData("rest([]);", null),
                new EvalIntegerArrayData("last([]);", null),
                new EvalIntegerArrayData("push([2], 3);", Arrays.asList(2, 3)),
                new EvalIntegerArrayData("first([]);", null)


        );
        for(EvalIntegerArrayData input : inputs) {
            var evaluated = testEval(input.input);
            if(input.value != null) {
                // TODO: This is incorrect for "push([], 2)" because it is an array being returned. Look into it.
                if(input.value.size() == 1) {
                    testIntegerObject(evaluated, input.value.get(0));
                } else {
                    testIntegerArrayObject(evaluated, input.value);
                }
            } else {
                testNullObject(evaluated);
            }
        }
    }

    @Test
    public void testHashLiterals() {
        String input = """
                let two = "two";
                {
                "one": 10 - 9,
                two: 1 + 1,
                "thr" + "ee": 6 / 2,
                4: 4,
                true: 5,
                false: 6
                }
                """;

        var evaluated = testEval(input);
        assertTrue(evaluated instanceof HashObject);
        HashObject hashObject = (HashObject) evaluated;
        assertEquals(hashObject.getPairs().get(new StringObject("one")), new IntegerObject(1));
        assertEquals(hashObject.getPairs().get(new StringObject("two")), new IntegerObject(2));
        assertEquals(hashObject.getPairs().get(new StringObject("three")), new IntegerObject(3));
        assertEquals(hashObject.getPairs().get(new IntegerObject(4)), new IntegerObject(4));
        assertEquals(hashObject.getPairs().get(new BooleanObject(true)), new IntegerObject(5));
        assertEquals(hashObject.getPairs().get(new BooleanObject(false)), new IntegerObject(6));
    }

    @Test
    public void testHashIndexExpressions() {
        List<EvalIntegerData> inputs = Arrays.asList(
                new EvalIntegerData("{\"foo\": 5}[\"foo\"]", 5),
                new EvalIntegerData("{\"foo\": 5}[\"bar\"]", null),
                new EvalIntegerData("let key = \"foo\"; {\"foo\": 5}[key]", 5),
                new EvalIntegerData("{}[\"foo\"]", null),
                new EvalIntegerData("{5: 5}[5]", 5),
                new EvalIntegerData("{true: 5}[true]", 5),
                new EvalIntegerData("{false: 5}[false]", 5)
        );
        for(EvalIntegerData input: inputs) {
            var evaluated = testEval(input.input);
            if(input.value != null) {
                testIntegerObject(evaluated, input.value);
            } else {
                testNullObject(evaluated);
            }
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

    private void testIntegerArrayObject(IObject object, List<Integer> elements) {
        assertTrue(object instanceof ArrayObject);
        ArrayObject arrayObject = (ArrayObject) object;
        assertEquals(arrayObject.getElementsLength(), elements.size());
        for (int i = 0; i < elements.size(); i++) {
            assertTrue(arrayObject.getElements().get(i) instanceof IntegerObject);
            IntegerObject integerObject = (IntegerObject) arrayObject.getElements().get(i);
            assertEquals(elements.get(i), integerObject.getValue());
        }
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
