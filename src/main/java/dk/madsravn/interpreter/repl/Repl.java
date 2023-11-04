package dk.madsravn.interpreter.repl;

import dk.madsravn.interpreter.ast.Program;
import dk.madsravn.interpreter.evaluator.Evaluator;
import dk.madsravn.interpreter.lexer.Lexer;
import dk.madsravn.interpreter.object.Environment;
import dk.madsravn.interpreter.object.IObject;
import dk.madsravn.interpreter.parser.Parser;
import dk.madsravn.interpreter.tokens.Token;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class Repl {
    private final String PROMPT = ">> ";

    public void start() {
        Environment env = new Environment();
        while(true) {
            System.out.print(PROMPT);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            try {
                String input = br.readLine();
                Lexer lexer = new Lexer(input);
                Parser parser = new Parser(lexer);
                Program program = parser.parseProgram();
                if (parser.getErrors().size() > 0) {
                    printParserErrors(parser.getErrors());
                    continue;
                }
                System.out.println(program.string());
                System.out.println("YIELDS:");
                IObject evaluated = Evaluator.evaluate(program, env);
                if (evaluated != null) {
                    System.out.println(evaluated.inspect());
                } else {
                    System.out.println("NOTHING");
                }
                System.out.println("");
            } catch (Exception e) {

            }

        }
    }

    private void printParserErrors(List<String> errors) {
        errors.stream().map(s -> "\t" + s).forEach(System.out::println);
    }
}
