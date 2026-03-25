package pythoncompiler;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class PythonCompiler {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());

            UIManager.put("nimbusBase", new java.awt.Color(50, 50, 50));
            UIManager.put("nimbusBlueGrey", new java.awt.Color(60, 60, 70));
            UIManager.put("control", new java.awt.Color(40, 44, 52));
            UIManager.put("text", new java.awt.Color(220, 220, 220));
            UIManager.put("nimbusLightBackground", new java.awt.Color(30, 30, 40));
            UIManager.put("info", new java.awt.Color(55, 118, 171));

        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Look & Feel error: " + ex.getMessage());
            }
        }

        SwingUtilities.invokeLater(() -> {
            CompilerGUI fenetre = new CompilerGUI();
            fenetre.setVisible(true);
        });
    }
}
