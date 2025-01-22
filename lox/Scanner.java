package lox.lox;

import static lox.lox.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    // fields to keep track of where the scanner is in the source code
    // start points to the first character in the lexeme being scanned
    private int start = 0;

    // current points to the character currently being considered
    private int current = 0;

    // the line tracks what source line current is on
    private int line = 1;

    // store the reserved keywords
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    // create the scanner based on a source string
    Scanner(String source) {
        this.source = source;
    }

    // this is a function that creates a list of tokens by looping through
    // all of the lexemes
    List<Token> scanTokens() {
        // iterate through entire file and scan all of tokens
        while (!isAtEnd()) {
            // we are at the beginning of the next lexeme
            start = current;
            scanToken();
        }

        // after all tokens are added, append an "end of file" token
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    // private helper function to recognize lexemes and scan tokens
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : EQUAL);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                // checking for a comment
                if (match('/')) {
                    // a comment goes until the end of the line
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else {
                    addToken(SLASH);
                }
                break;
            // less meaningful characters
            case ' ':
            case '\r':
            case '\t':
                break;
            // for adding a new line
            case '\n':
                line++;
                break;

            case '"':
                string();
                break;
            default:
                // to recognize number literals
                if (isDigit(c)) {
                    number();
                    // to recognize identifiers
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    // to find identifiers
    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();

        // check if the identifier is actually a reserved keyword
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);

        // if not a reserved keyword, then the type is an identifier
        if (type == null)
            type = IDENTIFIER;

        addToken(type);
    }

    // private method to recognize alphabet characters or underscore
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    // private method to see if characters are alphanumeric
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    // private method to recognize digits
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // private method to process a digit
    // we first consume all of the first numbers, then consume a ".", then consume
    // any following numbers after the dot
    private void number() {
        while (isDigit(peek()))
            advance();

        // look for a fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            // consume the "."
            advance();
            while (isDigit(peek()))
                advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    // private method to check the character after the current character
    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }

    // private method to call the analysis of a string
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            // this is to allow for multi-line strings
            if (peek() == '\n')
                line++;
            advance();
        }
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // consume the closing "
        advance();

        // trim the surrounding quotes and store the string literal
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    // this is like advance except that it doesn't consume the character, so we just
    // take a look at the next character, which is called lookahead
    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    // it's like a conditional advance(); if we're at the end or the next character
    // is not the expected character, then we return false, otherwise we consume the
    // next character
    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;
        current++;
        return true;
    }

    // some more helper methods to assist the scanToken()
    // advance() consumes the next character in source file
    private char advance() {
        return source.charAt(current++);
    }

    // addToken() is for output where it grabs the current lexeme and creates a new
    // token, and we'll use overloaded methods to deal with no literals and with
    // literals
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    // private helper function to know if we've consumed all of the characters
    private boolean isAtEnd() {
        return current >= source.length();
    }
}
