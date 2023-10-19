package dk.madsravn.interpreter;

import dk.madsravn.interpreter.lexer.Lexer;
import dk.madsravn.interpreter.repl.Repl;
import dk.madsravn.interpreter.tokens.Token;
import dk.madsravn.interpreter.tokens.TokenType;

import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Repl repl = new Repl();
        repl.start();
    }
}
