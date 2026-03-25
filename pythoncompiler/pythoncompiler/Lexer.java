package pythoncompiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/** Analyseur lexical pour un sous-ensemble du langage Python. */
public class Lexer {

    private static final int TAB_WIDTH = 4;

    private final String[] lines;
    private final Stack<Integer> indentStack = new Stack<>();

    public Lexer(String input) {
        // Gère aussi les fins de ligne Windows (\r\n) ; -1 conserve les lignes vides finales
        this.lines = input.split("\r?\n", -1);
        this.indentStack.push(0);
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        int lineNum = 1;

        for (String rawLine : lines) {
            String stripped = stripTrailing(rawLine);
            String trimmed  = stripped.trim();

            // Lignes vides et commentaires : ignorés
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                lineNum++;
                continue;
            }

            handleIndentation(measureIndent(stripped), lineNum, tokens);
            tokenizeLine(trimmed, lineNum, tokens);
            tokens.add(new Token(TokenType.NEWLINE, "\\n", lineNum));
            lineNum++;
        }

        // Ferme les blocs encore ouverts en fin de fichier
        while (indentStack.peek() > 0) {
            indentStack.pop();
            tokens.add(new Token(TokenType.DEDENT, "<DEDENT>", lineNum));
        }

        tokens.add(new Token(TokenType.EOF, "<EOF>", lineNum));
        return tokens;
    }

    private void handleIndentation(int indent, int lineNum, List<Token> tokens) {
        if (indent > indentStack.peek()) {
            indentStack.push(indent);
            tokens.add(new Token(TokenType.INDENT, "<INDENT>", lineNum));
        } else if (indent < indentStack.peek()) {
            while (indent < indentStack.peek()) {
                indentStack.pop();
                tokens.add(new Token(TokenType.DEDENT, "<DEDENT>", lineNum));
            }
            if (indent != indentStack.peek()) {
                tokens.add(new Token(TokenType.ERROR,
                    "Indentation incohérente à la ligne " + lineNum, lineNum));
            }
        }
    }

    private void tokenizeLine(String line, int lineNum, List<Token> tokens) {
        int pos = 0;
        int len = line.length();

        while (pos < len) {
            char c = line.charAt(pos);

            if (c == ' ' || c == '\t') {
                pos++;
            } else if (c == '#') {
                break; // commentaire de fin de ligne
            } else if (c == '"' || c == '\'') {
                pos = readString(line, pos, lineNum, tokens);
            } else if (c == ';') {
                // Séparateur d'instructions : équivaut à une fin de ligne logique
                tokens.add(new Token(TokenType.NEWLINE, "\\n", lineNum));
                pos++;
            } else if (Character.isLetter(c) || c == '_') {
                pos = readWord(line, pos, lineNum, tokens);
            } else if (Character.isDigit(c)) {
                pos = readNumber(line, pos, lineNum, tokens);
            } else {
                pos = readOperator(line, pos, lineNum, tokens);
            }
        }
    }

    private int readOperator(String line, int pos, int lineNum, List<Token> tokens) {
        if (pos + 1 < line.length()) {
            String two = line.substring(pos, pos + 2);
            TokenType type = twoCharToken(two);
            if (type != null) {
                tokens.add(new Token(type, two, lineNum));
                return pos + 2;
            }
        }

        char c = line.charAt(pos);
        TokenType type = singleCharToken(c);
        tokens.add(new Token(type != null ? type : TokenType.ERROR, String.valueOf(c), lineNum));
        return pos + 1;
    }

    private int readString(String line, int pos, int lineNum, List<Token> tokens) {
        char quote = line.charAt(pos++);
        StringBuilder sb = new StringBuilder();

        while (pos < line.length() && line.charAt(pos) != quote) {
            char c = line.charAt(pos);
            if (c == '\\' && pos + 1 < line.length()) {
                char esc = line.charAt(++pos);
                sb.append(esc == 'n' ? '\n' : esc == 't' ? '\t' : esc);
            } else {
                sb.append(c);
            }
            pos++;
        }

        if (pos >= line.length()) {
            tokens.add(new Token(TokenType.ERROR,
                "Chaîne de caractères non fermée", lineNum));
            return pos;
        }

        tokens.add(new Token(TokenType.STRING_LITERAL, sb.toString(), lineNum));
        return pos + 1; // saute le guillemet fermant
    }

    private int readWord(String line, int pos, int lineNum, List<Token> tokens) {
        int start = pos;
        while (pos < line.length()
               && (Character.isLetterOrDigit(line.charAt(pos)) || line.charAt(pos) == '_')) {
            pos++;
        }
        String word = line.substring(start, pos);
        tokens.add(new Token(resolveKeyword(word), word, lineNum));
        return pos;
    }

    private int readNumber(String line, int pos, int lineNum, List<Token> tokens) {
        int start = pos;
        boolean hasDot = false;

        while (pos < line.length()) {
            char c = line.charAt(pos);
            if (Character.isDigit(c)) {
                pos++;
            } else if (c == '.' && !hasDot && pos + 1 < line.length()
                       && Character.isDigit(line.charAt(pos + 1))) {
                hasDot = true;
                pos++;
            } else {
                break;
            }
        }

        tokens.add(new Token(TokenType.NUMBER, line.substring(start, pos), lineNum));
        return pos;
    }

    private TokenType twoCharToken(String s) {
        switch (s) {
            case "==": return TokenType.COMP_EQ;
            case "!=": return TokenType.COMP_NEQ;
            case "<=": return TokenType.COMP_LTE;
            case ">=": return TokenType.COMP_GTE;
            default:   return null;
        }
    }

    private TokenType singleCharToken(char c) {
        switch (c) {
            case '+': return TokenType.PLUS;
            case '-': return TokenType.MINUS;
            case '*': return TokenType.TIMES;
            case '/': return TokenType.DIVIDE;
            case '=': return TokenType.ASSIGN;
            case '<': return TokenType.COMP_LT;
            case '>': return TokenType.COMP_GT;
            case '(': return TokenType.LPAREN;
            case ')': return TokenType.RPAREN;
            case ':': return TokenType.COLON;
            case ',': return TokenType.COMMA;
            default:  return null;
        }
    }

    private TokenType resolveKeyword(String word) {
        switch (word) {
            case "if":     return TokenType.IF;
            case "elif":   return TokenType.ELIF;
            case "else":   return TokenType.ELSE;
            case "while":  return TokenType.WHILE;
            case "for":    return TokenType.FOR;
            case "in":     return TokenType.IN;
            case "return": return TokenType.RETURN;
            case "def":    return TokenType.DEF;
            case "print":  return TokenType.PRINT;
            case "and":    return TokenType.AND;
            case "or":     return TokenType.OR;
            case "not":    return TokenType.NOT;
            case "True":   return TokenType.TRUE;
            case "False":  return TokenType.FALSE;
            case "None":   return TokenType.NONE;
            default:       return TokenType.IDENTIFIER;
        }
    }

    private int measureIndent(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ')       count++;
            else if (c == '\t') count += TAB_WIDTH;
            else                break;
        }
        return count;
    }

    private String stripTrailing(String s) {
        int end = s.length();
        while (end > 0 && Character.isWhitespace(s.charAt(end - 1))) end--;
        return s.substring(0, end);
    }
}
