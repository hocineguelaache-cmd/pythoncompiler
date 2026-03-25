package pythoncompiler;

import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void parse() throws Exception {
        skipNewlines();
        while (!isAtEnd()) {
            instruction();
            skipNewlines();
        }
        // Si on arrive ici sans exception => code syntaxiquement correct
    }

    private void instruction() throws Exception {
        switch (peek().type) {
            case IF:    ifStmt();    break;
            case WHILE: whileStmt(); break;
            case FOR:   forStmt();   break;
            case DEF:   defStmt();   break;
            default:    instructionSimple(); break;
        }
    }

    private void instructionSimple() throws Exception {
        Token t = peek();

        if (t.type == TokenType.RETURN) {
            returnStmt();
        } else if (t.type == TokenType.PRINT) {
            printStmt();
        } else if (t.type == TokenType.IDENTIFIER
                   && lookahead(1).type == TokenType.ASSIGN) {
            affectation();
        } else {

            expression();
            consommerNewline();
        }
    }

    /** x = expression */
    private void affectation() throws Exception {
        match(TokenType.IDENTIFIER);
        match(TokenType.ASSIGN);
        expression();
        consommerNewline();
    }

    /** print(expression, ...) */
    private void printStmt() throws Exception {
        match(TokenType.PRINT);
        match(TokenType.LPAREN);
        if (!check(TokenType.RPAREN)) {
            expression();
            while (check(TokenType.COMMA)) {
                advance(); // consomme la virgule
                expression();
            }
        }
        match(TokenType.RPAREN);
        consommerNewline();
    }

    /** return [expression] */
    private void returnStmt() throws Exception {
        match(TokenType.RETURN);
        // L'expression après return est optionnelle
        if (!check(TokenType.NEWLINE) && !check(TokenType.EOF)
            && !check(TokenType.DEDENT)) {
            expression();
        }
        consommerNewline();
    }

    private void ifStmt() throws Exception {
        match(TokenType.IF);
        expression();
        matchColon();
        consommerNewline();
        bloc();

        while (check(TokenType.ELIF)) {
            advance();
            expression();
            matchColon();
            consommerNewline();
            bloc();
        }

        // else optionnel
        if (check(TokenType.ELSE)) {
            advance();
            matchColon();
            consommerNewline();
            bloc();
        }
    }

    private void whileStmt() throws Exception {
        match(TokenType.WHILE);
        expression();
        matchColon();
        consommerNewline();
        bloc();
    }

    private void forStmt() throws Exception {
        match(TokenType.FOR);
        if (peek().type != TokenType.IDENTIFIER) {
            throw new Exception("Erreur ligne " + peek().line
                + " : Un identifiant de variable est attendu après 'for'.");
        }
        match(TokenType.IDENTIFIER);
        match(TokenType.IN);
        expression();
        matchColon();
        consommerNewline();
        bloc();
    }

    private void defStmt() throws Exception {
        match(TokenType.DEF);
        if (peek().type != TokenType.IDENTIFIER) {
            throw new Exception("Erreur ligne " + peek().line
                + " : Un nom de fonction est attendu après 'def'.");
        }
        match(TokenType.IDENTIFIER);
        match(TokenType.LPAREN);

        if (!check(TokenType.RPAREN)) {
            match(TokenType.IDENTIFIER);
            while (check(TokenType.COMMA)) {
                advance();
                if (peek().type != TokenType.IDENTIFIER) {
                    throw new Exception("Erreur ligne " + peek().line
                        + " : Un paramètre (identifiant) est attendu après ','.");
                }
                match(TokenType.IDENTIFIER);
            }
        }

        match(TokenType.RPAREN);
        matchColon();
        consommerNewline();
        bloc();
    }

    private void bloc() throws Exception {
        if (!check(TokenType.INDENT)) {
            Token t = peek();
            throw new Exception("Erreur ligne " + t.line
                + " : Indentation attendue après ':'. "
                + "Vérifiez que le corps du bloc est bien indenté.");
        }
        match(TokenType.INDENT);
        skipNewlines();

        if (check(TokenType.DEDENT)) {
            throw new Exception("Erreur ligne " + peek().line
                + " : Le bloc est vide. "
                + "Au moins une instruction est requise après ':'.");
        }

        while (!check(TokenType.DEDENT) && !isAtEnd()) {
            instruction();
            skipNewlines();
        }

        match(TokenType.DEDENT);
    }

    /** expression => andExpr (OR andExpr)* */
    private void expression() throws Exception {
        andExpr();
        while (check(TokenType.OR)) {
            advance();
            andExpr();
        }
    }

    /** andExpr => notExpr (AND notExpr)* */
    private void andExpr() throws Exception {
        notExpr();
        while (check(TokenType.AND)) {
            advance();
            notExpr();
        }
    }

    /** notExpr => NOT notExpr | comparaison */
    private void notExpr() throws Exception {
        if (check(TokenType.NOT)) {
            advance();
            notExpr();
        } else {
            comparaison();
        }
    }

    /** comparaison => arithmétique (opComp arithmétique)? */
    private void comparaison() throws Exception {
        arithmetique();
        if (isOperateurComparaison()) {
            advance();
            arithmetique();
        }
    }

    /** arithmétique → terme ((PLUS | MINUS) terme)* */
    private void arithmetique() throws Exception {
        terme();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            advance();
            terme();
        }
    }

    /** terme => facteur ((TIMES | DIVIDE) facteur)* */
    private void terme() throws Exception {
        facteur();
        while (check(TokenType.TIMES) || check(TokenType.DIVIDE)) {
            advance();
            facteur();
        }
    }

    private void facteur() throws Exception {

        if (check(TokenType.MINUS)) {
            advance();
            facteur();
            return;
        }

        // Parenthèses groupantes
        if (check(TokenType.LPAREN)) {
            advance();
            expression();
            match(TokenType.RPAREN);
            return;
        }

        // Atome : nombre, chaîne, booléen, None, ou identifiant
        Token t = peek();
        switch (t.type) {
            case NUMBER:
            case STRING_LITERAL:
            case TRUE:
            case FALSE:
            case NONE:
                advance();
                break;

            case IDENTIFIER:
                advance();
                // Appel de fonction : ident(...)
                if (check(TokenType.LPAREN)) {
                    advance();
                    if (!check(TokenType.RPAREN)) {
                        expression();
                        while (check(TokenType.COMMA)) {
                            advance();
                            expression();
                        }
                    }
                    match(TokenType.RPAREN);
                }
                break;

            default:
                throw new Exception("Erreur ligne " + t.line
                    + " : Expression invalide. '"
                    + t.value + "' (" + t.type + ") est inattendu ici. "
                    + "Un nombre, une variable ou une expression était attendu.");
        }
    }

    private void skipNewlines() {
        while (check(TokenType.NEWLINE)) advance();
    }

    private void consommerNewline() throws Exception {
        if (check(TokenType.NEWLINE)) {
            advance();
        } else if (!isAtEnd()) {
            Token t = peek();
            throw new Exception("Erreur ligne " + t.line
                + " : Fin de ligne attendue après l'instruction, "
                + "mais '" + t.value + "' (" + t.type + ") trouvé.");
        }
    }

    /**
     * Correspond au COLON ':' avec un message d'erreur Python explicite.
     */
    private void matchColon() throws Exception {
        if (!check(TokenType.COLON)) {
            Token t = peek();
            throw new Exception("Erreur ligne " + t.line
                + " : Deux-points ':' attendu à la fin de la déclaration, "
                + "mais '" + t.value + "' trouvé.");
        }
        advance();
    }

    /** Correspond au token attendu ou lève une exception descriptive */
    private void match(TokenType expected) throws Exception {
        Token t = peek();
        if (t.type == expected) {
            advance();
        } else {
            throw new Exception("Erreur ligne " + t.line
                + " : " + describeExpected(expected)
                + ", mais '" + t.value + "' (" + t.type + ") trouvé.");
        }
    }

    /** Vérifie si le token courant est un opérateur de comparaison */
    private boolean isOperateurComparaison() {
        switch (peek().type) {
            case COMP_EQ:
            case COMP_LT:
            case COMP_GT:
            case COMP_LTE:
            case COMP_GTE:
            case COMP_NEQ:
                return true;
            default:
                return false;
        }
    }

    /** Retourne true si le token courant est du type donné */
    private boolean check(TokenType type) {
        return peek().type == type;
    }

    /** Avance d'un token et retourne le token consommé */
    private Token advance() {
        if (!isAtEnd()) current++;
        return tokens.get(current - 1);
    }

    /** Retourne le token courant sans consommer */
    private Token peek() {
        if (current < tokens.size()) return tokens.get(current);
        return new Token(TokenType.EOF, "<EOF>", -1);
    }

    /**
     * Regarde le token à N positions en avant sans consommer
     * (utilisé pour la distinction affectation/expression)
     */
    private Token lookahead(int offset) {
        int idx = current + offset;
        if (idx < tokens.size()) return tokens.get(idx);
        return new Token(TokenType.EOF, "<EOF>", -1);
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    /** Génère un message d'erreur lisible pour un type de token attendu */
    private String describeExpected(TokenType type) {
        switch (type) {
            case IDENTIFIER: return "Un identifiant (nom de variable ou de fonction) est attendu";
            case ASSIGN:     return "L'opérateur d'affectation '=' est attendu";
            case LPAREN:     return "Une parenthèse ouvrante '(' est attendue";
            case RPAREN:     return "Une parenthèse fermante ')' est attendue";
            case COLON:      return "Deux-points ':' attendu à la fin de la déclaration";
            case INDENT:     return "Une indentation est attendue (le bloc doit être indenté)";
            case DEDENT:     return "Une désindentation (DEDENT) est attendue";
            case NEWLINE:    return "Une fin de ligne est attendue";
            case IN:         return "Le mot-clé 'in' est attendu dans la boucle for";
            case DEF:        return "Le mot-clé 'def' est attendu pour définir une fonction";
            default:         return "'" + type + "' attendu";
        }
    }
}
