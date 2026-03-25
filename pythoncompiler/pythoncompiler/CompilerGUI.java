package pythoncompiler;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class CompilerGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color PY_BLUE      = new Color(55,  118, 171);
    private static final Color PY_BLUE_DARK = new Color(35,   85, 130);
    private static final Color PY_YELLOW    = new Color(255, 212,  59);
    private static final Color EDITOR_BG    = new Color(30,   30,  40);
    private static final Color EDITOR_FG    = new Color(220, 220, 220);
    private static final Color CONSOLE_BG   = new Color(18,   18,  24);
    private static final Color COLOR_OK     = new Color(80,  200,  80);
    private static final Color COLOR_ERR    = new Color(220,  80,  80);
    private static final Color COLOR_TOKEN  = new Color(180, 210, 255);
    private static final Color COLOR_INFO   = new Color(180, 180, 180);
    private static final Font FONT_CODE    = new Font("Monospaced", Font.PLAIN,  13);
    private static final Font FONT_HEADING = new Font("SansSerif",  Font.BOLD,   14);
    private static final Font FONT_LABEL   = new Font("SansSerif",  Font.BOLD,   12);
    private static final Font FONT_BTN     = new Font("SansSerif",  Font.BOLD,   12);
    private JTextArea   inputArea;
    private JTextPane   outputPane;
    private JButton     btnAnalyze;
    private JButton     btnClear;
    private JButton     btnExample;
    private JLabel      statusLabel;
    @SuppressWarnings("this-escape")
    public CompilerGUI() {
        setTitle("Mini-Compilateur Python — Université de Béjaïa");
        setSize(950, 720);
        setMinimumSize(new Dimension(750, 550));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        construireInterface();
    }

    private void construireInterface() {
        setLayout(new BorderLayout());

        add(creerEnTete(),   BorderLayout.NORTH);
        add(creerZoneCentrale(), BorderLayout.CENTER);
        add(creerBarreBas(), BorderLayout.SOUTH);
    }
    private JPanel creerEnTete() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PY_BLUE);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel titre = new JLabel("🐍  Mini-Compilateur Python");
        titre.setFont(new Font("SansSerif", Font.BOLD, 20));
        titre.setForeground(Color.WHITE);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        droite.setOpaque(false);

        JLabel univ = new JLabel("Université de Béjaïa");
        univ.setFont(new Font("SansSerif", Font.ITALIC, 12));
        univ.setForeground(PY_YELLOW);

        statusLabel = new JLabel("  Prêt");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(200, 255, 200));

        droite.add(statusLabel);
        droite.add(Box.createHorizontalStrut(20));
        droite.add(univ);

        header.add(titre, BorderLayout.WEST);
        header.add(droite, BorderLayout.EAST);

        return header;
    }

    private JSplitPane creerZoneCentrale() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                          creerPanneauEntree(),
                                          creerPanneauSortie());
        split.setDividerLocation(290);
        split.setDividerSize(7);
        split.setBorder(null);
        split.setContinuousLayout(true);
        return split;
    }

    private JPanel creerPanneauEntree() {
        inputArea = new JTextArea();
        inputArea.setFont(FONT_CODE);
        inputArea.setTabSize(4);
        inputArea.setBackground(EDITOR_BG);
        inputArea.setForeground(EDITOR_FG);
        inputArea.setCaretColor(PY_YELLOW);
        inputArea.setSelectionColor(PY_BLUE);
        inputArea.setSelectedTextColor(Color.WHITE);
        inputArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        inputArea.setLineWrap(false);

        inputArea.getInputMap().put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "analyser");
        inputArea.getActionMap().put("analyser", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { lancerAnalyse(); }
        });

        JScrollPane scroll = new JScrollPane(inputArea);
        scroll.setBorder(BorderFactory.createLineBorder(PY_BLUE_DARK, 2));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JLabel label = new JLabel("  Code Source Python :");
        label.setFont(FONT_LABEL);
        label.setBorder(new EmptyBorder(6, 4, 4, 0));

        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(new EmptyBorder(10, 10, 6, 10));
        panel.add(label,  BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel creerPanneauSortie() {
        outputPane = new JTextPane();
        outputPane.setEditable(false);
        outputPane.setBackground(CONSOLE_BG);
        outputPane.setFont(FONT_CODE);
        outputPane.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane scroll = new JScrollPane(outputPane);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 70), 2));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JLabel label = new JLabel("  Résultats de l'Analyse :");
        label.setFont(FONT_LABEL);
        label.setBorder(new EmptyBorder(4, 4, 4, 0));

        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(new EmptyBorder(6, 10, 6, 10));
        panel.add(label,  BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel creerBarreBas() {
        btnAnalyze = creerBouton("▶  Analyser le Code", PY_BLUE, Color.WHITE);
        btnAnalyze.addActionListener(e -> lancerAnalyse());

        btnClear = creerBouton("✕  Effacer", new Color(80, 80, 90), Color.WHITE);
        btnClear.addActionListener(e -> {
            inputArea.setText("");
            viderConsole();
            setStatus("Prêt", COLOR_INFO);
        });

        btnExample = creerBouton("☰  Exemple", new Color(70, 100, 70), Color.WHITE);
        btnExample.addActionListener(e -> chargerExemple());

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(new Color(30, 30, 38));
        bar.add(btnAnalyze);
        bar.add(btnClear);
        bar.add(btnExample);

        JLabel hint = new JLabel("  Ctrl+Entrée pour analyser");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(new Color(120, 120, 140));
        bar.add(Box.createHorizontalStrut(15));
        bar.add(hint);

        return bar;
    }

    private void lancerAnalyse() {
        String code = inputArea.getText();
        if (code.trim().isEmpty()) {
            viderConsole();
            appendConsole("⚠  Veuillez saisir du code Python avant d'analyser.\n",
                          new Color(255, 200, 60));
            setStatus("Aucun code", new Color(255, 200, 60));
            return;
        }

        viderConsole();
        setStatus("Analyse en cours...", COLOR_INFO);

        appendConsole("═══ ANALYSE LEXICALE ═══════════════════════════════\n\n",
                      PY_YELLOW);

        List<Token> tokens;
        try {
            Lexer lexer = new Lexer(code);
            tokens = lexer.tokenize();
        } catch (Exception ex) {
            appendConsole("✘  Erreur interne du lexer : " + ex.getMessage() + "\n", COLOR_ERR);
            setStatus("Erreur lexer", COLOR_ERR);
            return;
        }

        boolean aErreurLexicale = false;
        for (Token t : tokens) {
            if (t.type == TokenType.ERROR) {
                aErreurLexicale = true;
                appendConsole(t.toString() + "  ← ERREUR LEXICALE\n", COLOR_ERR);
            } else {
                appendConsole(t.toString() + "\n", couleurToken(t.type));
            }
        }

        appendConsole("\n═══ ANALYSE SYNTAXIQUE ══════════════════════════════\n\n",
                      PY_YELLOW);

        if (aErreurLexicale) {
            appendConsole("⚠  Des erreurs lexicales ont été détectées.\n"
                + "   Corrigez-les avant de lancer l'analyse syntaxique.\n", COLOR_ERR);
            setStatus("Erreur(s) lexicale(s)", COLOR_ERR);
            return;
        }

        try {
            Parser parser = new Parser(tokens);
            parser.parse();
            appendConsole("✔  Félicitations : Code syntaxiquement correct !\n", COLOR_OK);
            setStatus("✔  Syntaxe correcte", COLOR_OK);
        } catch (Exception ex) {
            appendConsole("✘  " + ex.getMessage() + "\n", COLOR_ERR);
            setStatus("✘  Erreur syntaxique", COLOR_ERR);
        }
    }

    private void chargerExemple() {
        String exemple =
            "# Affectations\n" +
            "x = 10\n" +
            "y = 3\n" +
            "nom = \"Python\"\n" +
            "\n" +
            "# Condition\n" +
            "if x > y:\n" +
            "    z = x + y\n" +
            "    print(z)\n" +
            "elif x == y:\n" +
            "    print(\"egaux\")\n" +
            "else:\n" +
            "    print(\"x est plus petit\")\n" +
            "\n" +
            "# Boucle while\n" +
            "compteur = 0\n" +
            "while compteur < 5:\n" +
            "    compteur = compteur + 1\n" +
            "\n" +
            "# Boucle for\n" +
            "for i in range(10):\n" +
            "    print(i)\n" +
            "\n" +
            "# Fonction\n" +
            "def calculer(a, b):\n" +
            "    resultat = a + b\n" +
            "    return resultat\n";

        inputArea.setText(exemple);
        inputArea.setCaretPosition(0);
        viderConsole();
        appendConsole("  Exemple chargé. Cliquez sur 'Analyser le Code' (ou Ctrl+Entrée).\n",
                      PY_YELLOW);
        setStatus("Exemple chargé", COLOR_INFO);
    }

    private void appendConsole(String text, Color color) {
        StyledDocument doc  = outputPane.getStyledDocument();
        String         name  = "style_" + color.getRGB();
        Style          style = outputPane.getStyle(name);
        if (style == null) {
            style = outputPane.addStyle(name, null);
            StyleConstants.setForeground(style, color);
            StyleConstants.setFontFamily(style, "Monospaced");
            StyleConstants.setFontSize(style, 13);
        }
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException ignored) {}
    }

    private void viderConsole() {
        outputPane.setText("");
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText("  " + message);
        statusLabel.setForeground(color);
    }

    private Color couleurToken(TokenType type) {
        switch (type) {
            case INDENT:
            case DEDENT:
            case NEWLINE:   return new Color(120, 180, 120);
            case NUMBER:    return new Color(210, 160, 255);
            case STRING_LITERAL: return new Color(255, 180, 100);
            case TRUE:
            case FALSE:
            case NONE:      return new Color(255, 140, 140);
            case IDENTIFIER:return EDITOR_FG;
            case IF: case ELIF: case ELSE:
            case WHILE: case FOR: case IN:
            case DEF: case RETURN:
            case PRINT: case AND: case OR: case NOT:
                            return new Color(100, 180, 255);
            case ASSIGN:
            case PLUS: case MINUS: case TIMES: case DIVIDE:
            case COMP_EQ: case COMP_LT: case COMP_GT:
            case COMP_LTE: case COMP_GTE: case COMP_NEQ:
                            return new Color(255, 212, 59);
            case EOF:       return COLOR_INFO;
            case ERROR:     return COLOR_ERR;
            default:        return EDITOR_FG;
        }
    }

    private JButton creerBouton(String label, Color fond, Color texte) {
        JButton btn = new JButton(label);
        btn.setFont(FONT_BTN);
        btn.setBackground(fond);
        btn.setForeground(texte);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        Color hoverColor = fond.brighter();
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hoverColor); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(fond); }
        });
        return btn;
    }
}
