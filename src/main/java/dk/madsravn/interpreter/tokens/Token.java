package dk.madsravn.interpreter.tokens;
//TOOD: Rename to TokenType
public class Token {
    private final TokenType type;
    private final String literal;

    public Token(TokenType type, String literal) {
        this.type = type;
        this.literal = literal;
    }
    public TokenType getType() {
        return type;
    }

    public String getLiteral() {
        return literal;
    }

    @Override
    public String toString() {
        return "[" + type + " ==> " + literal + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Token)) {
            return false;
        }
        Token c = (Token) o;

        return c.getType().equals(this.getType()) && c.getLiteral().equals(this.getLiteral());
    }

    public static TokenType lookUpIdentifier(String type) {
        switch (type) {
            case "fn":
                return TokenType.FUNCTION;
            case "let":
                return TokenType.LET;
            case "true":
                return TokenType.TRUE;
            case "false":
                return TokenType.FALSE;
            case "if":
                return TokenType.IF;
            case "else":
                return TokenType.ELSE;
            case "return":
                return TokenType.RETURN;
            default:
                return TokenType.IDENT;
        }
    }
}
