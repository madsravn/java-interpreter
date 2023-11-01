package dk.madsravn.interpreter.evaluator;

import dk.madsravn.interpreter.ast.Program;
import dk.madsravn.interpreter.lexer.Lexer;
import dk.madsravn.interpreter.object.BooleanObject;
import dk.madsravn.interpreter.object.IObject;
import dk.madsravn.interpreter.object.IntegerObject;
import dk.madsravn.interpreter.parser.Parser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static dk.madsravn.interpreter.evaluator.Evaluator.evaluate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EvaluatorTest {

    private record EvalIntegerData(String input, int value) { }
    private record EvalBooleanData(String input, boolean value) { }

    @Test
    public void testEvalIntegerExpression() {
        List<EvalIntegerData> inputs = Arrays.asList(
                new EvalIntegerData("5", 5),
                new EvalIntegerData("10", 10)
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
                new EvalBooleanData("false", false)
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

        return evaluate(program);
    }
}
