package dk.madsravn.interpreter.parser;

import dk.madsravn.interpreter.ast.*;
import dk.madsravn.interpreter.lexer.Lexer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        assertEquals(program.getStatements().get(0).tokenLiteral(), "let");
        assertEquals(program.getStatements().get(1).tokenLiteral(), "let");
        assertEquals(program.getStatements().get(2).tokenLiteral(), "let");

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

        Program program = parser.parseProgram();
        assertEquals(parser.getErrors().size(), 5);
    }

    @Test
    public void testStringMethods() {
        String input = "let myVar = 5;";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkForParseErrors(parser, input);

        assertEquals(program.string(), "let myVar = 5;");
    }

    @Test
    public void testIdentifierExpression() {
        String input = "foobar;";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkForParseErrors(parser, input);

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
    private record InfixDataBoolean(String input, boolean leftValue, String operator, boolean rightValue) { }

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

    private record InfixDataInteger(String input, int leftValue, String operator, int rightValue) { }

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

    private record PrefixDataBoolean(String input, String operator, boolean value) { }

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

    private record PrefixDataInteger(String input, String operator, int integerValue) { }

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

    private record OperatorPrecedenceParsing(String input, String expected) { }

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
                new OperatorPrecedenceParsing("!(true == true)", "(!(true == true))"),
                new OperatorPrecedenceParsing("a + add(b * c) + d", "((a + add((b * c))) + d)"),
                new OperatorPrecedenceParsing("add(a, b, 1, 2 * 3, 4 + 5, add(6, 7 * 8))", "add(a, b, 1, (2 * 3), (4 + 5), add(6, (7 * 8)))"),
                new OperatorPrecedenceParsing("add(a + b + c * d / f + g)", "add((((a + b) + ((c * d) / f)) + g))"),
                new OperatorPrecedenceParsing("a * [1, 2, 3, 4][b * c] * d", "((a * ([1, 2, 3, 4][(b * c)])) * d)"),
                new OperatorPrecedenceParsing("add(a * b[2], b[1], 2 * [1, 2][1])", "add((a * (b[2])), (b[1]), (2 * ([1, 2][1])))")
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

    public void testIdentifier(IExpression expression, String value) {
        assertTrue(expression instanceof Identifier);
        Identifier identifier = (Identifier) expression;
        assertEquals(identifier.getValue(), value);
        assertEquals(identifier.tokenLiteral(), value);
    }

    @Test
    public void testIfExpression() {
        String input = "if (x < y) { x }";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkForParseErrors(parser, input);

        assertEquals(program.getStatementsLength(), 1);
        assertTrue(program.getStatements().get(0) instanceof ExpressionStatement);
        ExpressionStatement expressionStatement = (ExpressionStatement) program.getStatements().get(0);

        assertTrue(expressionStatement.getExpression() instanceof IfExpression);
        IfExpression ifExpression = (IfExpression) expressionStatement.getExpression();

        assertTrue(ifExpression.getCondition() instanceof InfixExpression);
        InfixExpression infixExpression = (InfixExpression) ifExpression.getCondition();
        assertEquals(infixExpression.getOperator(), "<");

        testIdentifier(infixExpression.getLeft(), "x");
        testIdentifier(infixExpression.getRight(), "y");

        assertEquals(ifExpression.getConsequence().getStatementsLength(), 1);
        assertTrue(ifExpression.getConsequence().getStatements().get(0) instanceof ExpressionStatement);
        ExpressionStatement consequence = (ExpressionStatement) ifExpression.getConsequence().getStatements().get(0);
        testIdentifier(consequence.getExpression(), "x");
        assertNull(ifExpression.getAlternative());

    }

    public void testIfElseExpression() {
        String input = "if (x < y) { x } else { y }";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkForParseErrors(parser, input);

        assertEquals(program.getStatementsLength(), 1);
        assertTrue(program.getStatements().get(0) instanceof ExpressionStatement);
        ExpressionStatement expressionStatement = (ExpressionStatement) program.getStatements().get(0);

        assertTrue(expressionStatement.getExpression() instanceof IfExpression);
        IfExpression ifExpression = (IfExpression) expressionStatement.getExpression();

        assertTrue(ifExpression.getCondition() instanceof InfixExpression);
        InfixExpression infixExpression = (InfixExpression) ifExpression.getCondition();
        assertEquals(infixExpression.getOperator(), "<");

        testIdentifier(infixExpression.getLeft(), "x");
        testIdentifier(infixExpression.getRight(), "y");

        assertEquals(ifExpression.getConsequence().getStatementsLength(), 1);
        assertTrue(ifExpression.getConsequence().getStatements().get(0) instanceof ExpressionStatement);
        ExpressionStatement consequence = (ExpressionStatement) ifExpression.getConsequence().getStatements().get(0);
        testIdentifier(consequence.getExpression(), "x");

        assertEquals(ifExpression.getAlternative().getStatementsLength(), 1);
        assertTrue(ifExpression.getAlternative().getStatements().get(0) instanceof ExpressionStatement);
        ExpressionStatement alternative = (ExpressionStatement) ifExpression.getAlternative().getStatements().get(0);
        testIdentifier(alternative.getExpression(), "y");
    }

    @Test
    public void testFuntionLiteralParsing() {
        String input = "fn(x, y) { x + y; }";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkForParseErrors(parser, input);

        assertEquals(program.getStatementsLength(), 1);
        assertTrue(program.getStatements().get(0) instanceof ExpressionStatement);

        ExpressionStatement expressionStatement = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(expressionStatement.getExpression() instanceof FunctionLiteral);

        FunctionLiteral functionLiteral = (FunctionLiteral) expressionStatement.getExpression();
        assertEquals(functionLiteral.getParametersLength(), 2);

        testIdentifier(functionLiteral.getParameters().get(0), "x");
        testIdentifier(functionLiteral.getParameters().get(1), "y");

        assertEquals(functionLiteral.getBody().getStatementsLength(), 1);

        assertTrue(functionLiteral.getBody().getStatements().get(0) instanceof ExpressionStatement);
        ExpressionStatement bodyStatement = (ExpressionStatement) functionLiteral.getBody().getStatements().get(0);

        assertTrue(bodyStatement.getExpression() instanceof InfixExpression);
        InfixExpression infixExpression = (InfixExpression) bodyStatement.getExpression();
        testIdentifier(infixExpression.getLeft(), "x");
        testIdentifier(infixExpression.getRight(), "y");
        assertEquals(infixExpression.getOperator(), "+");
    }

    private record FunctionParameterData(String input, List<String> parameters) { }

    @Test
    public void testFuntionParameterParsing() {
        List<FunctionParameterData> inputs = Arrays.asList(
                new FunctionParameterData("fn() {};", Arrays.asList()),
                new FunctionParameterData("fn(x) {};", Arrays.asList("x")),
                new FunctionParameterData("fn(x, y) {};", Arrays.asList("x", "y")),
                new FunctionParameterData("fn(x, y, z) {};", Arrays.asList("x", "y", "z"))
        );

        for(FunctionParameterData input : inputs) {
            Lexer lexer = new Lexer(input.input);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();
            checkForParseErrors(parser, input.input);

            assertEquals(program.getStatementsLength(), 1);
            assertTrue(program.getStatements().get(0) instanceof ExpressionStatement);
            ExpressionStatement expressionStatement = (ExpressionStatement) program.getStatements().get(0);

            assertTrue(expressionStatement.getExpression() instanceof FunctionLiteral);
            FunctionLiteral functionLiteral = (FunctionLiteral) expressionStatement.getExpression();

            assertEquals(functionLiteral.getParametersLength(), input.parameters.size());
            for(int i = 0; i < input.parameters.size(); i++) {
                testIdentifier(functionLiteral.getParameters().get(i), input.parameters.get(i));
            }
        }
    }

    @Test
    public void testCallExpressionParsing() {
        String input = "add(1, 2 * 3, 4 + 5);";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkForParseErrors(parser, input);

        assertEquals(program.getStatementsLength(), 1);
        assertTrue(program.getStatements().get(0) instanceof ExpressionStatement);
        ExpressionStatement expressionStatement = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(expressionStatement.getExpression() instanceof CallExpression);
        CallExpression callExpression = (CallExpression) expressionStatement.getExpression();

        testIdentifier(callExpression.getFunction(), "add");
        assertEquals(callExpression.getArguments().size(), 3);

        testIntegerLiteral(callExpression.getArguments().get(0), 1);
        testIntegerInfixExpression(callExpression.getArguments().get(1), 2, "*", 3);
        testIntegerInfixExpression(callExpression.getArguments().get(2), 4, "+", 5);

    }

    @Test
    public void testStringLiteralExpression() {
        String input = """
                "hello world";
                """;
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkForParseErrors(parser, input);

        assertTrue(program.getStatements().get(0) instanceof ExpressionStatement);
        ExpressionStatement expressionStatement = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(expressionStatement.getExpression() instanceof StringLiteral);
        StringLiteral stringLiteral = (StringLiteral) expressionStatement.getExpression();
        assertEquals(stringLiteral.getValue(), "hello world");

    }

    @Test
    public void testParsingArrayLiterals() {
        String input = "[1, 2 * 2, 3 + 3]";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkForParseErrors(parser, input);
        assertEquals(program.getStatementsLength(), 1);
        assertTrue(program.getStatements().get(0) instanceof ExpressionStatement);
        ExpressionStatement expressionStatement = (ExpressionStatement) program.getStatements().get(0);

        assertTrue(expressionStatement.getExpression() instanceof ArrayLiteral);
        ArrayLiteral arrayLiteral = (ArrayLiteral) expressionStatement.getExpression();
        assertEquals(arrayLiteral.getElementsLength(), 3);

        testIntegerLiteral(arrayLiteral.getElements().get(0), 1);
        testIntegerInfixExpression(arrayLiteral.getElements().get(1), 2, "*", 2);
        testIntegerInfixExpression(arrayLiteral.getElements().get(2), 3, "+", 3);
    }

    @Test
    public void testParsingIndexExpressions() {
        String input = "myArray[1 + 1]";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkForParseErrors(parser, input);

        assertEquals(program.getStatementsLength(), 1);
        assertTrue(program.getStatements().get(0) instanceof ExpressionStatement);

        ExpressionStatement expressionStatement = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(expressionStatement.getExpression() instanceof IndexExpression);
        IndexExpression indexExpression = (IndexExpression) expressionStatement.getExpression();
        testIdentifier(indexExpression.getLeft(), "myArray");
        testIntegerInfixExpression(indexExpression.getIndex(), 1, "+", 1);
    }

    @Test
    public void testParsingHashLiteralStringKeys() {
        String input = """
                {"one": 1, "two": 2, "three": 3}
                """;
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkForParseErrors(parser, input);

        Map<String, Integer> expected = Map.of("one", 1, "two", 2, "three", 3);
        Map<String, Integer> actual = new HashMap<String, Integer>();

        assertEquals(program.getStatementsLength(), 1);
        assertTrue(program.getStatements().get(0) instanceof ExpressionStatement);
        ExpressionStatement expressionStatement = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(expressionStatement.getExpression() instanceof  HashLiteral);
        HashLiteral hashLiteral = (HashLiteral) expressionStatement.getExpression();
        assertEquals(hashLiteral.getPairsLength(), 3);
        for (Map.Entry<IExpression, IExpression> expression: hashLiteral.getPairs().entrySet()) {
            assertTrue(expression.getKey() instanceof StringLiteral);
            assertTrue(expression.getValue() instanceof IntegerLiteral);
            StringLiteral stringLiteral = (StringLiteral) expression.getKey();
            IntegerLiteral integerLiteral = (IntegerLiteral) expression.getValue();
            actual.put(stringLiteral.getValue(), integerLiteral.getValue());
        }
        assertEquals(expected, actual);
    }

    @Test
    public void testParsingEmptyHashLiteral() {
        String input = "{}";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkForParseErrors(parser, input);


        assertEquals(program.getStatementsLength(), 1);
        assertTrue(program.getStatements().get(0) instanceof ExpressionStatement);
        ExpressionStatement expressionStatement = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(expressionStatement.getExpression() instanceof  HashLiteral);
        HashLiteral hashLiteral = (HashLiteral) expressionStatement.getExpression();
        assertEquals(hashLiteral.getPairsLength(), 0);
    }

    @Test
    public void testParsingHashLiteralWithExpressions() {
        String input = """
                {"one": 0 + 1, "two": 10 - 8, "three": 15 / 5}
                """;
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        checkForParseErrors(parser, input);

        Map<String, IExpression> actual = new HashMap<String, IExpression>();

        assertEquals(program.getStatementsLength(), 1);
        assertTrue(program.getStatements().get(0) instanceof ExpressionStatement);
        ExpressionStatement expressionStatement = (ExpressionStatement) program.getStatements().get(0);
        assertTrue(expressionStatement.getExpression() instanceof  HashLiteral);
        HashLiteral hashLiteral = (HashLiteral) expressionStatement.getExpression();
        assertEquals(hashLiteral.getPairsLength(), 3);
        for (Map.Entry<IExpression, IExpression> expression: hashLiteral.getPairs().entrySet()) {
            assertTrue(expression.getKey() instanceof StringLiteral);
            StringLiteral stringLiteral = (StringLiteral) expression.getKey();
            actual.put(stringLiteral.getValue(), expression.getValue());
        }
        testIntegerInfixExpression(actual.get("one"), 0, "+", 1);
        testIntegerInfixExpression(actual.get("two"), 10, "-", 8);
        testIntegerInfixExpression(actual.get("three"), 15, "/", 5);
    }

    public void testIntegerInfixExpression(IExpression expression, int left, String operator, int right) {
        assertTrue(expression instanceof InfixExpression);
        InfixExpression infixExpression = (InfixExpression) expression;
        testIntegerLiteral(infixExpression.getLeft(), left);
        testIntegerLiteral(infixExpression.getRight(), right);
        assertEquals(infixExpression.getOperator(), operator);
    }

    public void testIntegerLiteral(IExpression expression, int value) {
        assertTrue(expression instanceof IntegerLiteral);
        IntegerLiteral integerLiteral = (IntegerLiteral) expression;
        assertEquals(integerLiteral.getValue(), value);
        assertEquals(integerLiteral.tokenLiteral(), "" + value);
    }

    private void checkForParseErrors(Parser parser, String input) {
        assertEquals(parser.getErrors().size(), 0, "Input [" + input + "] should not gives errors. There should not be any errors: " + formatErrors(parser.getErrors()));
    }

    private String formatErrors(List<String> errors) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for ( String error : errors) {
            sb.append(error + "\n");
        }
        return sb.toString();
    }
}
