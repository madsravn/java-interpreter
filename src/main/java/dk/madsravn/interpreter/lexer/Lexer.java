package dk.madsravn.interpreter.lexer;

import dk.madsravn.interpreter.tokens.Token;
import dk.madsravn.interpreter.tokens.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private static final char NOTHING = '\0';
    private final String input;
    private int position;
    private int readPosition;
    private char ch;

    public Lexer(String input) {
        this.input = input;
        position = 0;
        readPosition = 0;
        ch = NOTHING;
        readChar();
    }

    public void readChar() {
        if (readPosition >= input.length()) {
            ch = NOTHING;
        } else {
            ch = input.charAt(readPosition);
        }
        position = readPosition;
        readPosition += 1;
    }

    public static Token eofToken() {
        return new Token(TokenType.EOF, "" + NOTHING);
    }

    private void skipWhitespace() {
        while(ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
            readChar();
        }
    }

    private Token findToken() {
        skipWhitespace();
        switch (ch) {
            case '"':
                String content = readString();
                return new Token(TokenType.STRING, content);
            case ':':
                return new Token(TokenType.COLON, ":");
            case '=':
                if (peekChar() == '=') {
                    char current = ch;
                    readChar();
                    String literal = "" + current + ch;
                    return new Token(TokenType.EQ, literal);
                } else {
                    return new Token(TokenType.ASSIGN, "=");
                }
            case '+':
                return new Token(TokenType.PLUS, "+");
            case '-':
                return new Token(TokenType.MINUS, "-");
            case '!':
                if (peekChar() == '=') {
                    char current = ch;
                    readChar();
                    String literal = "" + current + ch;
                    return new Token(TokenType.NOT_EQ, literal);
                } else {
                    return new Token(TokenType.BANG, "!");
                }
            case '*':
                return new Token(TokenType.ASTERISK, "*");
            case '/':
                return new Token(TokenType.SLASH, "/");
            case '<':
                return new Token(TokenType.LT, "<");
            case '>':
                return new Token(TokenType.GT, ">");
            case ',':
                return new Token(TokenType.COMMA, ",");
            case ';':
                return new Token(TokenType.SEMICOLON, ";");
            case '(':
                return new Token(TokenType.LPAREN, "(");
            case ')':
                return new Token(TokenType.RPAREN, ")");
            case '{':
                return new Token(TokenType.LBRACE, "{");
            case '}':
                return new Token(TokenType.RBRACE, "}");
            case '[':
                return new Token(TokenType.LBRACKET, "[");
            case ']':
                return new Token(TokenType.RBRACKET, "]");
            case NOTHING:
                return new Token(TokenType.EOF, "" + NOTHING);
            default:
                if (isLetter(ch)) {
                    String identifier = readIdentifier();
                    TokenType token = Token.lookUpIdentifier(identifier);
                    return new Token(token, identifier);
                } else if (isDigit(ch)) {
                    String number = readNumber();
                    return new Token(TokenType.INT, number);
                } else {
                    return new Token(TokenType.ILLEGAL, "" + ch);
                }
        }
    }

    private String readString() {
        int startPosition = position + 1;
        // TODO: This is ugly. Fix
        while(true) {
            readChar();
            if(ch == '"' || ch == NOTHING) {
                break;
            }
        }
        return input.substring(startPosition, position);
    }

    private String readIdentifier() {
        int startPosition = position;
        while(isLetter(ch)) {
            readChar();
        }

        return input.substring(startPosition, position);
    }

    private boolean isLetter(char ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || ch == '_';
    }

    private boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }

    private String readNumber() {
        int startPosition = position;
        while(isDigit(ch)) {
            readChar();
        }

        return input.substring(startPosition, position);
    }

    protected boolean advanceToken(Token token) {
        List<TokenType> tokenTypes = List.of(TokenType.IDENT, TokenType.FUNCTION, TokenType.INT, TokenType.LET,
                TokenType.TRUE, TokenType.FALSE, TokenType.IF, TokenType.ELSE, TokenType.RETURN);

        return !tokenTypes.contains(token.getType());
    }

    private char peekChar() {
        if (readPosition >= input.length()) {
            return NOTHING;
        } else {
            return input.charAt(readPosition);
        }
    }

    public Token nextToken() {
        Token token = findToken();
        if (advanceToken(token)) {
            readChar();
        }

        return token;
    }

    // TODO: Create and make pretty - could be cool if we could stream this
    public List<Token> readAllTokens() {
        List<Token> tokens = new ArrayList<Token>();
        while(true) {
            Token token = nextToken();
            tokens.add(token);
            if (token.getType() == TokenType.EOF) {
                break;
            }
        }

        return tokens;
    }
}
