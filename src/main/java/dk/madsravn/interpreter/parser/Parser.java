package dk.madsravn.interpreter.parser;

import dk.madsravn.interpreter.ast.*;
import dk.madsravn.interpreter.lexer.Lexer;
import dk.madsravn.interpreter.tokens.Token;
import dk.madsravn.interpreter.tokens.TokenType;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            case PLUS, MINUS, SLASH, ASTERISK, EQ, NOT_EQ, LT, GT, LPAREN, LBRACKET:
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
            case IF:
                return parseIfExpression();
            case FUNCTION:
                return parseFunctionLiteral();
            case STRING:
                return parseStringLiteral();
            case LBRACKET:
                return parseArrayLiteral();
            case LBRACE:
                return parseHashLiteral();
            default:
                noPrefixParseFunctionError(currentToken.getType());
                // TODO: This is ugly
                return null;
        }
    }

    private IExpression parseStringLiteral() {
        return new StringLiteral(currentToken, currentToken.getLiteral());
    }

    private IExpression parseHashLiteral() {
        Token token = currentToken;
        Map<IExpression, IExpression> pairs = new HashMap<>();
        while(!peekTokenType(RBRACE)) {
            nextToken();
            var key = parseExpression(LOWEST);

            if(!expectPeekType(COLON)) {
                return null;
            }

            nextToken();
            var value = parseExpression(LOWEST);
            pairs.put(key, value);

            if(!peekTokenType(RBRACE) && !expectPeekType(COMMA)) {
                return null;
            }
        }

        if(!expectPeekType(RBRACE)) {
            return null;
        }

        return new HashLiteral(token, pairs);
    }

    private IExpression parseArrayLiteral() {
        return new ArrayLiteral(currentToken, parseExpressionList(RBRACKET));
    }

    private List<IExpression> parseExpressionList(TokenType end) {
        List<IExpression> expressions = new ArrayList<IExpression>();
        if(peekTokenType(end)) {
            nextToken();
            return expressions;
        }
        nextToken();
        expressions.add(parseExpression(LOWEST));

        while(peekTokenType(COMMA)) {
            nextToken();
            nextToken();
            expressions.add(parseExpression(LOWEST));
        }

        if(!expectPeekType(end)) {
            return null;
        }

        return expressions;
    }

    private IExpression parseCallExpression(IExpression function) {
        CallExpression callExpression = new CallExpression(currentToken, function, parseExpressionList(RPAREN));
        return callExpression;
    }

    private IExpression parseFunctionLiteral() {
        Token token = currentToken;
        if(!expectPeekType(LPAREN)) {
            return null;
        }
        List<Identifier> parameters = parseFunctionParameters();
        if(!expectPeekType(LBRACE)) {
            return null;
        }
        BlockStatement body = parseBlockStatement();
        return new FunctionLiteral(token, parameters, body);
    }

    private List<Identifier> parseFunctionParameters() {
        List<Identifier> parameters = new ArrayList<Identifier>();

        if(peekTokenType(RPAREN)) {
            nextToken();
            return parameters;
        }

        nextToken();

        Identifier identifier = new Identifier(currentToken, currentToken.getLiteral());
        parameters.add(identifier);

        while(peekTokenType(COMMA)) {
            nextToken();
            nextToken();
            identifier = new Identifier(currentToken, currentToken.getLiteral());
            parameters.add(identifier);
        }

        if (!expectPeekType(RPAREN)) {
            return null;
        }

        return parameters;
    }

    private IExpression parseGroupedExpression() {
        nextToken();
        IExpression expression = parseExpression(LOWEST);
        if (!expectPeekType(RPAREN)) {
            return null;
        }
        return expression;
    }

    private IExpression parseIfExpression() {
        Token token = currentToken;
        if (!expectPeekType(LPAREN)) {
            return null;
        }

        nextToken();
        IExpression condition = parseExpression(LOWEST);

        if (!expectPeekType(RPAREN)) {
            return null;
        }

        if (!expectPeekType(LBRACE)) {
            return null;
        }
        BlockStatement consequence = parseBlockStatement();
        BlockStatement alternative = parseAlternativeBlock();

        IfExpression ifExpression = new IfExpression(token, condition, consequence, alternative);
        return  ifExpression;
    }

    private BlockStatement parseAlternativeBlock() {
        if (peekTokenType(ELSE)) {
            nextToken();
            if (!expectPeekType(LBRACE)) {
                return null;
            }
            return parseBlockStatement();
        }
        return null;
    }

    private BlockStatement parseBlockStatement() {
        Token token = currentToken;
        List<IStatement> statements = new ArrayList<IStatement>();

        nextToken();

        while (!currentTokenType(RBRACE) && !currentTokenType(TokenType.EOF)) {
            IStatement statement = parseStatement();
            if (statement != null) {
                statements.add(statement);
            }
            nextToken();
        }
        return new BlockStatement(token, statements);
    }

    private IExpression parseExpression(PrecedenceEnum precedence) {
        IExpression left = getPrefixExpression();
        if (left == null) {
            noPrefixParseFunctionError(currentToken.getType());
            return null;
        }

        while (!peekTokenType(TokenType.SEMICOLON) && precedence.compareTo(peekPrecedence()) < 0)  {
            boolean infix = getInfixExpression(peekToken.getType());
            if (infix == false) {
                return null;
            }
            // TODO: This needs to be prettier!!!
            if(peekToken.getType() == LPAREN) {
                nextToken();
                left = parseCallExpression(left);

            } else if (peekToken.getType() == LBRACKET) {
                nextToken();
                left = parseIndexExpression(left);
            }
            else {

                nextToken();
                left = parseInfixExpression(left);
            }
        }

        return left;
    }

    private IndexExpression parseIndexExpression(IExpression left) {
        Token token = currentToken;
        nextToken();
        IExpression index = parseExpression(LOWEST);
        if(!expectPeekType(RBRACKET)) {
            return null;
        }
        return new IndexExpression(token, left, index);
    }

    private PrecedenceEnum getPrecedence(TokenType type) {
        switch (type) {
            case LBRACKET:
                return INDEX;
            case LPAREN:
                return CALL;
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
        Token token = currentToken;
        nextToken();

        IExpression value = parseExpression(LOWEST);

        while(!currentTokenType(TokenType.SEMICOLON)) {
            nextToken();
        }

        ReturnStatement statement = new ReturnStatement(token, value);
        return statement;
    }

    private LetStatement parseLetStatement() {
        Token token = currentToken;

        if (!expectPeekType(TokenType.IDENT)) {
            // TODO: This is ugly
            return null;
        }

        // TODO: Remove method and replace with constructor call to LetStatement
        Identifier name = new Identifier(currentToken, currentToken.getLiteral());

        if (!expectPeekType(TokenType.ASSIGN)) {
            // TODO: This is ugly
            return null;
        }

        nextToken();
        IExpression value = parseExpression(LOWEST);


        while (!currentTokenType(TokenType.SEMICOLON)) {
            nextToken();
        }

        LetStatement statement = new LetStatement(token, name, value);
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
