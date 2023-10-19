package dk.madsravn.interpreter.parser;

import dk.madsravn.interpreter.ast.*;
import dk.madsravn.interpreter.lexer.Lexer;
import org.junit.jupiter.api.Test;

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
        checkForParseErrors(parser);

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
        checkForParseErrors(parser);


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
        assertEquals(parser.getErrors().size(), 3);
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
        checkForParseErrors(parser);

        assertEquals(program.getStatementsLength(), 1);
        IStatement statement = program.getStatements().get(0);
        assertTrue(statement instanceof ExpressionStatement);
        IExpression expression = ((ExpressionStatement)statement).getExpression();

        assertTrue(expression instanceof IntegerLiteral);
        IntegerLiteral integerLiteral = (IntegerLiteral)expression;
        assertEquals(integerLiteral.getValue(), 5);
        assertEquals(integerLiteral.tokenLiteral(), "5");
    }

    private void checkForParseErrors(Parser parser) {
        assertEquals(parser.getErrors().size(), 0, "There should not be any errors: " + parser.getErrors());
    }
}
