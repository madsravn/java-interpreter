package dk.madsravn.interpreter.lexer;

import dk.madsravn.interpreter.tokens.Token;
import dk.madsravn.interpreter.tokens.TokenType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class LexerTest {
    @Test
    public void testNextToken() {
        String input = "=+(){},;";
        List<Token> tokens = List.of(
                new Token(TokenType.ASSIGN, "="),
                new Token(TokenType.PLUS, "+"),
                new Token(TokenType.LPAREN, "("),
                new Token(TokenType.RPAREN, ")"),
                new Token(TokenType.LBRACE, "{"),
                new Token(TokenType.RBRACE, "}"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.SEMICOLON, ";"),
                Lexer.eofToken()
        );
        Lexer lexer = new Lexer(input);
        IntStream.range(0, tokens.size()).forEach(i -> assertEquals(tokens.get(i), lexer.nextToken()));
    }

    @Test
    public void textNextTokenToAllTokens() {
        String input = "=+(){},;";
        Lexer lexer = new Lexer(input);
        List<Token> actual = lexer.readAllTokens();
        Lexer lexer_expected = new Lexer(input);
        IntStream.range(0, actual.size()).forEach(i -> assertEquals(actual.get(i), lexer_expected.nextToken()));
    }

    @Test
    public void textExtendedNextToken() {
        String input = """
                        let five = 5;
                        let ten = 10;
                        
                        let add = fn(x, y) {
                            x + y;
                        };
                        
                        let result = add(five, ten);
                        !-/*5;
                        5 < 10 > 5;
                        
                        if (5 < 10) {
                            return true;
                        } else {
                            return false;
                        }
                        
                        10 == 10;
                        10 != 9;
                        "foobar"
                        "foo bar"
                        [1, 2];
                        {"foo": "bar"}
                        """;
        List<Token> tokens = List.of(
                new Token(TokenType.LET, "let"),
                new Token(TokenType.IDENT, "five"),
                new Token(TokenType.ASSIGN, "="),
                new Token(TokenType.INT, "5"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.LET, "let"),
                new Token(TokenType.IDENT, "ten"),
                new Token(TokenType.ASSIGN, "="),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.LET, "let"),
                new Token(TokenType.IDENT, "add"),
                new Token(TokenType.ASSIGN, "="),
                new Token(TokenType.FUNCTION, "fn"),
                new Token(TokenType.LPAREN, "("),
                new Token(TokenType.IDENT, "x"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.IDENT, "y"),
                new Token(TokenType.RPAREN, ")"),
                new Token(TokenType.LBRACE, "{"),
                new Token(TokenType.IDENT, "x"),
                new Token(TokenType.PLUS, "+"),
                new Token(TokenType.IDENT, "y"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.RBRACE, "}"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.LET, "let"),
                new Token(TokenType.IDENT, "result"),
                new Token(TokenType.ASSIGN, "="),
                new Token(TokenType.IDENT, "add"),
                new Token(TokenType.LPAREN, "("),
                new Token(TokenType.IDENT, "five"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.IDENT, "ten"),
                new Token(TokenType.RPAREN, ")"),
                new Token(TokenType.SEMICOLON, ";"),

                new Token(TokenType.BANG, "!"),
                new Token(TokenType.MINUS, "-"),
                new Token(TokenType.SLASH, "/"),
                new Token(TokenType.ASTERISK, "*"),
                new Token(TokenType.INT, "5"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.INT, "5"),
                new Token(TokenType.LT, "<"),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.GT, ">"),
                new Token(TokenType.INT, "5"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.IF, "if"),
                new Token(TokenType.LPAREN, "("),
                new Token(TokenType.INT, "5"),
                new Token(TokenType.LT, "<"),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.RPAREN, ")"),
                new Token(TokenType.LBRACE, "{"),
                new Token(TokenType.RETURN, "return"),
                new Token(TokenType.TRUE, "true"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.RBRACE, "}"),
                new Token(TokenType.ELSE, "else"),
                new Token(TokenType.LBRACE, "{"),
                new Token(TokenType.RETURN, "return"),
                new Token(TokenType.FALSE, "false"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.RBRACE, "}"),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.EQ, "=="),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.INT, "10"),
                new Token(TokenType.NOT_EQ, "!="),
                new Token(TokenType.INT, "9"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.STRING, "foobar"),
                new Token(TokenType.STRING, "foo bar"),
                new Token(TokenType.LBRACKET, "["),
                new Token(TokenType.INT, "1"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.INT, "2"),
                new Token(TokenType.RBRACKET, "]"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.LBRACE, "{"),
                new Token(TokenType.STRING, "foo"),
                new Token(TokenType.COLON, ":"),
                new Token(TokenType.STRING, "bar"),
                new Token(TokenType.RBRACE, "}"),

                Lexer.eofToken()
        );

        Lexer lexer = new Lexer(input);
        List<Token> actual = lexer.readAllTokens();
        assertEquals(tokens.size(), actual.size());
        assertEquals(tokens, actual, () -> "Expected and actual lists are not equal: \n " + tokens + " \n " + actual + "\n ");

    }

    @Test
    public void testNextTokenWithAll() {
        String input = "=+(){},;";
        List<Token> tokens = List.of(
                new Token(TokenType.ASSIGN, "="),
                new Token(TokenType.PLUS, "+"),
                new Token(TokenType.LPAREN, "("),
                new Token(TokenType.RPAREN, ")"),
                new Token(TokenType.LBRACE, "{"),
                new Token(TokenType.RBRACE, "}"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.SEMICOLON, ";"),
                Lexer.eofToken()
        );
        Lexer lexer = new Lexer(input);
        List<Token> actual = lexer.readAllTokens();
        assertEquals(tokens.size(), actual.size());
        assertEquals(tokens, actual, () -> "Expected and actual lists are not equal: \n " + tokens + " \n " + actual + "\n ");
    }

    @Test
    public void testTokenTypesThatAdvanceReadChar() {
        String input = "=+(){},;";
        Lexer lexer = new Lexer(input);
        // TODO: Creates tests for all TokenTypes
        assertEquals(lexer.advanceToken(new Token(TokenType.ASSIGN, "=")), true);
        assertEquals(lexer.advanceToken(new Token(TokenType.FUNCTION, "fn")), false);

    }
}
