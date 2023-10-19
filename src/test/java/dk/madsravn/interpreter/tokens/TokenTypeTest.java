package dk.madsravn.interpreter.tokens;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TokenTypeTest {
    @Test
    public void testEquality() {
        Token tokenOne = new Token(TokenType.ASSIGN, "=");
        Token tokenTwo = new Token(TokenType.ASSIGN, "=");
        assertEquals(tokenOne, tokenTwo);
    }

    @Test
    public void testIneqaulity() {
        Token tokenOne = new Token(TokenType.ASSIGN, "=");
        Token tokenTwo = new Token(TokenType.ASSIGN, ";");
        Token tokenThree = new Token(TokenType.RBRACE, "}");
        Token tokenFour = new Token(TokenType.RBRACE, "=");
        assertNotEquals(tokenOne, tokenTwo);
        assertNotEquals(tokenOne, tokenThree);
        assertNotEquals(tokenOne, tokenFour);
        assertNotEquals(tokenTwo, tokenThree);
        assertNotEquals(tokenTwo, tokenFour);
        assertNotEquals(tokenThree, tokenFour);
    }
}
