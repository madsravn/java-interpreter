package dk.madsravn.interpreter.parser;

import dk.madsravn.interpreter.ast.*;
import dk.madsravn.interpreter.lexer.Lexer;
import dk.madsravn.interpreter.tokens.Token;
import dk.madsravn.interpreter.tokens.TokenType;

import java.util.ArrayList;
import java.util.List;

import static dk.madsravn.interpreter.parser.PrecedenceEnum.*;
import static dk.madsravn.interpreter.tokens.TokenType.*;

public class Parser {
    private Token currentToken;
    private Token peekToken;
    private final Lexer lexer;
    private final List<String> errors;
    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.errors = new ArrayList<String>();
        nextToken();
        nextToken();

    }

    private void nextToken() {
        currentToken = peekToken;
        peekToken = lexer.nextToken();
    }

    public Program parseProgram() {
        Program program = new Program();
        while (currentToken.getType() != TokenType.EOF) {
            IStatement statement = parseStatement();
            if (statement != null) {
                program.addStatement(statement);
            }
            nextToken();
        }
        return program;
    }

    public List<String> getErrors() {
        return errors;
    }

    private void peekError(TokenType type) {
        errors.add("Expected next token to be " + type + " but got " + peekToken.getType() + " instead.");
    }

    private IStatement parseStatement() {
        switch (currentToken.getType()) {
            case LET:
                return parseLetStatement();
            case RETURN:
                return parseReturnStatement();
            default:
                return parseExpressionStatement();
        }
    }

    private boolean getInfixExpression(TokenType type) {
        switch(type) {
            case PLUS, MINUS, SLASH, ASTERISK, EQ, NOT_EQ, LT, GT:
                return true;
            default:
                return false;

        }
    }

    private IExpression getPrefixExpression() {
        switch(currentToken.getType()) {
            case IDENT:
                return parseIdentifier();
            case INT:
                return parseIntegerLiteral();
            case BANG, MINUS:
                return parsePrefixExpression();
            case TRUE, FALSE:
                return parseBoolean();
            case LPAREN:
                return parseGroupedExpression();
            default:
                noPrefixParseFunctionError(currentToken.getType());
                // TODO: This is ugly
                return null;
        }
    }

    private IExpression parseGroupedExpression() {
        nextToken();
        IExpression expression = parseExpression(LOWEST);
        if (!expectPeekType(RPAREN)) {
            return null;
        }
        return expression;
    }

    private IExpression parseExpression(PrecedenceEnum precedence) {
        IExpression left = getPrefixExpression();
        if (left == null) {
            return null;
        }

        while (!peekTokenType(TokenType.SEMICOLON) && precedence.compareTo(peekPrecedence()) < 0)  {
            boolean infix = getInfixExpression(peekToken.getType());
            if (infix == false) {
                return null;
            }

            nextToken();

            left = parseInfixExpression(left);
        }

        return left;
    }

    private PrecedenceEnum getPrecedence(TokenType type) {
        switch (type) {
            case EQ, NOT_EQ:
                return EQUALS;
            case LT, GT:
                return LESSGREATER;
            case PLUS, MINUS:
                return SUM;
            case SLASH, ASTERISK:
                return PRODUCT;
            default:
                return LOWEST;
        }
    }

    private PrecedenceEnum peekPrecedence() {
        return getPrecedence(peekToken.getType());
    }

    private PrecedenceEnum currentPrecedence() {
        return getPrecedence(currentToken.getType());
    }

    private IExpression parseIdentifier() {
        return new Identifier(currentToken, currentToken.getLiteral());
    }

    private IExpression parsePrefixExpression() {
        Token token = currentToken;
        nextToken();
        IExpression right = parseExpression(PrecedenceEnum.PREFIX);
        return new PrefixExpression(token, token.getLiteral(), right);
    }

    private IExpression parseInfixExpression(IExpression left) {
        Token token = currentToken;
        PrecedenceEnum precedence = currentPrecedence();
        nextToken();
        IExpression right = parseExpression(precedence);
        return new InfixExpression(token, left, token.getLiteral(), right);
    }

    private BooleanType parseBoolean() {
        return new BooleanType(currentToken, currentTokenType(TokenType.TRUE));
    }

    private void noPrefixParseFunctionError(TokenType tokenType) {
        errors.add("No prefix parse function found for " + tokenType);
    }

    private IntegerLiteral parseIntegerLiteral() {
        try {
            int integerValue = Integer.parseInt(currentToken.getLiteral());
            IntegerLiteral integerLiteral = new IntegerLiteral(currentToken, integerValue);
            return integerLiteral;
        } catch (NumberFormatException nm) {
            // TODO: This is ugly
            return null;
        }
    }
    private ExpressionStatement parseExpressionStatement() {
        ExpressionStatement statement = new ExpressionStatement(currentToken);
        statement.setExpression(parseExpression(PrecedenceEnum.LOWEST));

        if (peekTokenType(TokenType.SEMICOLON)) {
            nextToken();
        }
        return statement;
    }

    private ReturnStatement parseReturnStatement() {
        ReturnStatement statement = new ReturnStatement(currentToken);
        nextToken();

        while(!currentTokenType(TokenType.SEMICOLON)) {
            nextToken();
        }
        return statement;
    }

    private LetStatement parseLetStatement() {
        LetStatement statement = new LetStatement(currentToken);

        if (!expectPeekType(TokenType.IDENT)) {
            // TODO: This is ugly
            return null;
        }

        statement.setName(new Identifier(currentToken, currentToken.getLiteral()));

        if (!expectPeekType(TokenType.ASSIGN)) {
            // TODO: This is ugly
            return null;
        }

        while (!currentTokenType(TokenType.SEMICOLON)) {
            nextToken();
        }

        return statement;
    }

    private boolean currentTokenType(TokenType type) {
        return currentToken.getType() == type;
    }

    private boolean peekTokenType(TokenType type) {
        return peekToken.getType() == type;
    }

    private boolean expectPeekType(TokenType type) {
        if (peekTokenType(type)) {
            nextToken();
            return true;
        } else {
            peekError(type);
            return false;
        }
    }
}
