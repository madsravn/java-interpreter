package dk.madsravn.interpreter.parser;

import dk.madsravn.interpreter.ast.*;
import dk.madsravn.interpreter.lexer.Lexer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {
    @Test
    public void TestLetStatements() {
        String input = """
                let x = 5;
                let y = 10;
                let foobar = 838383;
                """;
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        checkForParseErrors(parser, input);

        Program program = parser.parseProgram();
        assertNotNull(program, "program is null");
        assertEquals(program.getStatementsLength(), 3, "Program does not contain 3 statements, but " + program.getStatementsLength());
        for(IStatement statement : program.getStatements()) {
            assertTrue(statement instanceof LetStatement);
        }

        assertEquals(((LetStatement)program.getStatements().get(0)).getName().getValue(), "x", "First LET identifier expected to be 'x', but was " + ((LetStatement)program.getStatements().get(0)).getName().getValue());
        assertEquals(((LetStatement)program.getStatements().get(1)).getName().getValue(), "y", "Second LET identifier expected to be 'x', but was " + ((LetStatement)program.getStatements().get(1)).getName().getValue());
        assertEquals(((LetStatement)program.getStatements().get(2)).getName().getValue(), "foobar", "Third LET identifier expected to be 'x', but was " + ((LetStatement)program.getStatements().get(2)).getName().getValue());

        assertEquals(program.getStatements().get(0).tokenLiteral(), "let", "First LET identifier expected to be 'x', but was " + program.getStatements().get(0).tokenLiteral());
        assertEquals(program.getStatements().get(1).tokenLiteral(), "let", "Second LET identifier expected to be 'x', but was " + program.getStatements().get(1).tokenLiteral());
        assertEquals(program.getStatements().get(2).tokenLiteral(), "let", "Third LET identifier expected to be 'x', but was " + program.getStatements().get(2).tokenLiteral());

    }

    @Test
    public void TestReturnStatements() {
        String input = """
                return 5;
                return 10;
                return 838383;
                """;
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        checkForParseErrors(parser, input);


        Program program = parser.parseProgram();
        assertNotNull(program, "program is null");
        assertEquals(program.getStatementsLength(), 3, "Program does not contain 3 statements, but " + program.getStatementsLength());

        assertEquals(program.getStatements().get(0).tokenLiteral(), "return");
        assertEquals(program.getStatements().get(1).tokenLiteral(), "return");
        assertEquals(program.getStatements().get(2).tokenLiteral(), "return");
    }

    @Test
    public void TestLetStatementWithErrors() {
        String input = """
                let x 5;
                let = 10;
                let 838383;
                """;
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);

        // TODO: Should the errors be on the program or on the parser?
        Program program = parser.parseProgram();
        assertEquals(parser.getErrors().size(), 4);
    }

    @Test
    public void testStringMethods() {
        String input = "let myVar = 5;";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertEquals(parser.getErrors().size(), 0);
        // TODO: THIS DOESNT COMPLETELY WORK YET
        assertEquals(program.string(), "let myVar = ;");
    }

    @Test
    public void testIdentifierExpression() {
        String input = "foobar;";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);

        Program program = parser.parseProgram();
        assertEquals(parser.getErrors().size(), 0);

        assertEquals(program.getStatementsLength(), 1);
        IStatement statement = program.getStatements().get(0);

        assertTrue(statement instanceof ExpressionStatement);
        IExpression e = ((ExpressionStatement)statement).getExpression();

        assertTrue(e instanceof Identifier);
        Identifier i = (Identifier) e;

        assertEquals(i.tokenLiteral(), "foobar");
        assertEquals(i.getValue(), "foobar");
    }

    @Test
    public void testIntegerLiteralExpression() {
        String input = "5;";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkForParseErrors(parser, input);

        assertEquals(program.getStatementsLength(), 1);
        IStatement statement = program.getStatements().get(0);
        assertTrue(statement instanceof ExpressionStatement);
        IExpression expression = ((ExpressionStatement)statement).getExpression();

        assertTrue(expression instanceof IntegerLiteral);
        IntegerLiteral integerLiteral = (IntegerLiteral)expression;
        assertEquals(integerLiteral.getValue(), 5);
        assertEquals(integerLiteral.tokenLiteral(), "5");
    }



    // TODO: InfixDataBoolean and InfixDataInteger needs to be merged somehow
    private class InfixDataBoolean {
        public String input;
        public String operator;
        public boolean leftValue;
        public boolean rightValue;
        public InfixDataBoolean(String input, boolean leftValue, String operator, boolean rightValue) {
            this.input = input;
            this.leftValue = leftValue;
            this.operator = operator;
            this.rightValue = rightValue;
        }
    }

    @Test
    public void testParsingBooleanInfixExpressions() {
        List<InfixDataBoolean> inputs = Arrays.asList(
                new InfixDataBoolean("true == true;", true, "==", true),
                new InfixDataBoolean("true == false;", true, "==", false),
                new InfixDataBoolean("false == true;", false, "==", true),
                new InfixDataBoolean("false == false;", false, "==", false),
                new InfixDataBoolean("true != true;", true, "!=", true),
                new InfixDataBoolean("true != false;", true, "!=", false),
                new InfixDataBoolean("false != true;", false, "!=", true),
                new InfixDataBoolean("false != false;", false, "!=", false)
        );
        for(InfixDataBoolean infixData : inputs) {
            Lexer lexer = new Lexer(infixData.input);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();
            checkForParseErrors(parser, infixData.input);

            assertEquals(program.getStatementsLength(), 1);
            assertTrue(program.getStatements().get(0) instanceof ExpressionStatement);
            ExpressionStatement statement = (ExpressionStatement) program.getStatements().get(0);
            assertTrue(statement.getExpression() instanceof InfixExpression);
            InfixExpression infixExpression = (InfixExpression) statement.getExpression();
            assertEquals(infixExpression.getOperator(), infixData.operator);

            // TODO: testIntegerLiteral method for later
            assertTrue(infixExpression.getRight() instanceof BooleanType);
            BooleanType rightBooleanLiteral = (BooleanType) infixExpression.getRight();

            assertEquals(rightBooleanLiteral.getValue(), infixData.rightValue);
            assertEquals(rightBooleanLiteral.tokenLiteral(), "" + infixData.rightValue);

            assertTrue(infixExpression.getLeft() instanceof BooleanType);
            BooleanType leftBooleanLiteral = (BooleanType) infixExpression.getLeft();

            assertEquals(leftBooleanLiteral.getValue(), infixData.leftValue);
            assertEquals(leftBooleanLiteral.tokenLiteral(), "" + infixData.leftValue);
        }

    }

    private class InfixDataInteger {
        public String input;
        public String operator;
        public int leftValue;
        public int rightValue;
        public InfixDataInteger(String input, int leftValue, String operator, int rightValue) {
            this.input = input;
            this.leftValue = leftValue;
            this.operator = operator;
            this.rightValue = rightValue;
        }
    }

    @Test
    public void testParsingIntegerInfixExpressions() {
        List<InfixDataInteger> inputs = Arrays.asList(
                new InfixDataInteger("5 + 5;", 5, "+", 5),
                new InfixDataInteger("5 - 5;", 5, "-", 5),
                new InfixDataInteger("5 * 5;", 5, "*", 5),
                new InfixDataInteger("5 / 5;", 5, "/", 5),
                new InfixDataInteger("5 > 5;", 5, ">", 5),
                new InfixDataInteger("5 < 5;", 5, "<", 5),
                new InfixDataInteger("5 == 5;", 5, "==", 5),
                new InfixDataInteger("5 != 5;", 5, "!=", 5)
        );
        for(InfixDataInteger infixData : inputs) {
            Lexer lexer = new Lexer(infixData.input);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();
            checkForParseErrors(parser, infixData.input);

            assertEquals(program.getStatementsLength(), 1);
            assertTrue(program.getStatements().get(0) instanceof ExpressionStatement);
            ExpressionStatement statement = (ExpressionStatement) program.getStatements().get(0);
            assertTrue(statement.getExpression() instanceof InfixExpression);
            InfixExpression infixExpression = (InfixExpression) statement.getExpression();
            assertEquals(infixExpression.getOperator(), infixData.operator);

            // TODO: testIntegerLiteral method for later
            assertTrue(infixExpression.getRight() instanceof IntegerLiteral);
            IntegerLiteral rightIntegerLiteral = (IntegerLiteral) infixExpression.getRight();

            assertEquals(rightIntegerLiteral.getValue(), infixData.rightValue);
            assertEquals(rightIntegerLiteral.tokenLiteral(), "" + infixData.rightValue);

            assertTrue(infixExpression.getLeft() instanceof IntegerLiteral);
            IntegerLiteral leftIntegerLiteral = (IntegerLiteral) infixExpression.getLeft();

            assertEquals(leftIntegerLiteral.getValue(), infixData.leftValue);
            assertEquals(leftIntegerLiteral.tokenLiteral(), "" + infixData.leftValue);
        }

    }

    private class PrefixDataBoolean
    {
        public String input;
        public String operator;
        public boolean value;
        public PrefixDataBoolean(String input, String operator, boolean value) {
            this.input = input;
            this.operator = operator;
            this.value = value;
        }
    }

    @Test
    public void testParsingBooleanPrefixExpression() {
        List<PrefixDataBoolean> inputs = Arrays.asList(
                new PrefixDataBoolean("!true;", "!", true),
                new PrefixDataBoolean("!false;", "!", false));
        for(PrefixDataBoolean prefixData : inputs) {
            Lexer lexer = new Lexer(prefixData.input);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();
            checkForParseErrors(parser, prefixData.input);

            assertEquals(program.getStatementsLength(), 1);
            assertTrue(program.getStatements().get(0) instanceof ExpressionStatement);
            ExpressionStatement statement = (ExpressionStatement) program.getStatements().get(0);
            assertTrue(statement.getExpression() instanceof PrefixExpression);
            PrefixExpression prefixExpression = (PrefixExpression) statement.getExpression();
            assertEquals(prefixExpression.getOperator(), prefixData.operator);

            // TODO: testIntegerLiteral method for later
            assertTrue(prefixExpression.getRight() instanceof BooleanType);
            BooleanType booleanLiteral = (BooleanType) prefixExpression.getRight();

            assertEquals(booleanLiteral .getValue(), prefixData.value);
            assertEquals(booleanLiteral .tokenLiteral(), "" + prefixData.value);
        }
    }

    private class PrefixDataInteger
    {
        public String input;
        public String operator;
        public int integerValue;
        public PrefixDataInteger(String input, String operator, int integerValue) {
            this.input = input;
            this.operator = operator;
            this.integerValue = integerValue;
        }
    }

    @Test
    public void testParsingIntegerPrefixExpression() {
        List<PrefixDataInteger> inputs = Arrays.asList(
                new PrefixDataInteger("!5;", "!", 5),
                new PrefixDataInteger("-15;", "-", 15));
        for(PrefixDataInteger prefixData : inputs) {
            Lexer lexer = new Lexer(prefixData.input);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();
            checkForParseErrors(parser, prefixData.input);

            assertEquals(program.getStatementsLength(), 1);
            assertTrue(program.getStatements().get(0) instanceof ExpressionStatement);
            ExpressionStatement statement = (ExpressionStatement) program.getStatements().get(0);
            assertTrue(statement.getExpression() instanceof PrefixExpression);
            PrefixExpression prefixExpression = (PrefixExpression) statement.getExpression();
            assertEquals(prefixExpression.getOperator(), prefixData.operator);

            // TODO: testIntegerLiteral method for later
            assertTrue(prefixExpression.getRight() instanceof IntegerLiteral);
            IntegerLiteral integerLiteral = (IntegerLiteral) prefixExpression.getRight();

            assertEquals(integerLiteral.getValue(), prefixData.integerValue);
            assertEquals(integerLiteral.tokenLiteral(), "" + prefixData.integerValue);
        }
    }

    private class OperatorPrecedenceParsing {
        public String input;
        public String expected;
        public OperatorPrecedenceParsing(String input, String expected) {
            this.input = input;
            this.expected = expected;
        }
    }

    @Test
    public void testOperatorPrecedenceParsing() {
        List<OperatorPrecedenceParsing> operatorPrecedenceParsingList = Arrays.asList(
                new OperatorPrecedenceParsing("-a * b", "((-a) * b)"),
                new OperatorPrecedenceParsing("!-a", "(!(-a))"),
                new OperatorPrecedenceParsing("a + b + c", "((a + b) + c)"),
                new OperatorPrecedenceParsing("a + b - c", "((a + b) - c)"),
                new OperatorPrecedenceParsing("a * b * c", "((a * b) * c)"),
                new OperatorPrecedenceParsing("a * b / c", "((a * b) / c)"),
                new OperatorPrecedenceParsing("a + b / c", "(a + (b / c))"),
                new OperatorPrecedenceParsing("a + b * c + d / e - f", "(((a + (b * c)) + (d / e)) - f)"),
                new OperatorPrecedenceParsing("3 + 4; -5 * 5", "(3 + 4)((-5) * 5)"),
                new OperatorPrecedenceParsing("5 < 4 == 3 < 4", "((5 < 4) == (3 < 4))"),
                new OperatorPrecedenceParsing("5 > 4 != 3 > 4", "((5 > 4) != (3 > 4))"),
                new OperatorPrecedenceParsing("3 + 4 * 5 == 3 * 1 + 4 * 5", "((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))"),
                new OperatorPrecedenceParsing("true", "true"),
                new OperatorPrecedenceParsing("false", "false"),
                new OperatorPrecedenceParsing("3 > 5 == false", "((3 > 5) == false)"),
                new OperatorPrecedenceParsing("3 < 5 == true", "((3 < 5) == true)"),
                new OperatorPrecedenceParsing("1 + (2 + 3) + 4", "((1 + (2 + 3)) + 4)"),
                new OperatorPrecedenceParsing("(5 + 5) * 2", "((5 + 5) * 2)"),
                new OperatorPrecedenceParsing("2 / (5 + 5)", "(2 / (5 + 5))"),
                new OperatorPrecedenceParsing("-(5 + 5)", "(-(5 + 5))"),
                new OperatorPrecedenceParsing("!(true == true)", "(!(true == true))")

        );

        for (OperatorPrecedenceParsing operatorPrecedenceParsing : operatorPrecedenceParsingList) {
            String input = operatorPrecedenceParsing.input;
            Lexer lexer = new Lexer(input);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();
            checkForParseErrors(parser, operatorPrecedenceParsing.input);

            assertEquals(program.string(), operatorPrecedenceParsing.expected);
        }
    }

    private void checkForParseErrors(Parser parser, String input) {
        assertEquals(parser.getErrors().size(), 0, "Input [" + input + "] should not gives errors. There should not be any errors: " + parser.getErrors());
    }
}
