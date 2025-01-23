package craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            // receive a file path and read it
            runFile(args[0]);
        } else {
            // open an interative prompt to read input
            runPrompt();
        }
    }

    // run interpreter by inputting a file and reading through it
    private static void runFile(String path) throws IOException {
        // read all bytes in the file and call the run function
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError)
            System.exit(65);
    }

    // run interpreter by starting a prompt and receiving user input
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        // read through each input line and use run function
        for (;;) {
            System.out.println("> ");
            String line = reader.readLine();
            if (line == null)
                break;
            run(line);
            hadError = false;
        }
    }

    // private run function used to scan for tokens in the text
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // old method for printing out all of the tokens
        /*
         * for (Token token : tokens) {
         * System.out.println(token);
         * }
         */

        // new method using the parser to print out syntax tree
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        // stop if there was a syntax error
        if (hadError)
            return;

        System.out.println(new AstPrinter().print(expression));
    }

    // error function that outputs an error message
    static void error(int line, String message) {
        report(line, "", message);
    }

    // reporting errors
    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    // another error() function that outputs error at a given token location
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}