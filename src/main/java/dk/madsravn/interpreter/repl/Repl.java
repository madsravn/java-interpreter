package dk.madsravn.interpreter.repl;

import dk.madsravn.interpreter.lexer.Lexer;
import dk.madsravn.interpreter.tokens.Token;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class Repl {
    private final String PROMPT = ">> ";

    public void start() {
        while(true) {
            System.out.print(PROMPT);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            try {
                String input = br.readLine();
                Lexer lexer = new Lexer(input);
                List<Token> tokens = lexer.readAllTokens();
                tokens.stream().forEach(System.out::println);
            } catch (Exception e) {

            }

        }
    }
}
