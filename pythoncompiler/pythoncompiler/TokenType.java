package pythoncompiler;

public enum TokenType {

    //  Tokens structurels Python (indentation)
    NEWLINE,        // Fin de ligne logique
    INDENT,         // Augmentation du niveau d'indentation
    DEDENT,         // Diminution du niveau d'indentation

    //  Littéraux
    IDENTIFIER,     // Nom de variable ou de fonction (ex: x, ma_var)
    NUMBER,         // Entier ou flottant   (ex: 42, 3.14)
    STRING_LITERAL, // Chaîne de caractères (ex: "bonjour", 'monde')

    //  Opérateurs arithmétiques
    ASSIGN,         // =
    PLUS,           // +
    MINUS,          // -
    TIMES,          // *
    DIVIDE,         // /

    //  Opérateurs de comparaison
    COMP_EQ,        // ==
    COMP_LT,        // <
    COMP_GT,        // >
    COMP_LTE,       // <=
    COMP_GTE,       // >=
    COMP_NEQ,       // !=

    //  Opérateurs logiques
    AND,            // and
    OR,             // or
    NOT,            // not

    // Délimiteurs
    LPAREN,         // (
    RPAREN,         // )
    COLON,          // :
    COMMA,          // ,

    //  Mots-clés de contrôle
    IF,             // if
    ELIF,           // elif
    ELSE,           // else
    WHILE,          // while
    FOR,            // for
    IN,             // in
    RETURN,         // return

    //  Mots-clés de définition
    DEF,            // def
    PRINT,          // print

    //  Valeurs littérales Python
    TRUE,           // True
    FALSE,          // False
    NONE,           // None

    //  Tokens spéciaux
    EOF,            // Fin du fichier source
    ERROR           // Caractère ou séquence non reconnu(e)
}
