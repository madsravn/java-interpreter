package dk.madsravn.interpreter.evaluator;

import dk.madsravn.interpreter.ast.Program;
import dk.madsravn.interpreter.lexer.Lexer;
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

    private void testIntegerObject(IObject object, int expected) {
        assertTrue(object instanceof IntegerObject);
        IntegerObject integerObject = (IntegerObject) object;
        assertEquals(integerObject.getValue(), expected);
    }

    private IObject testEval(String input) {
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        return evaluate(program);
    }
}
