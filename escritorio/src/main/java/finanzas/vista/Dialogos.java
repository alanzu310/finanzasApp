package finanzas.vista;

import javax.swing.*;
import java.awt.*;

/** Cuadros de diálogo reutilizables. */
public final class Dialogos {
    private Dialogos() {}

    public static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    public static void info(Component padre, String titulo, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmar(Component padre, String titulo, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, titulo, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** @return el texto escrito, o null si se canceló */
    public static String pedir(Component padre, String mensaje) {
        return JOptionPane.showInputDialog(padre, mensaje);
    }

    /**
     * Muestra un formulario con una etiqueta por cada campo.
     *
     * @return true si se pulsó Aceptar
     */
    public static boolean formulario(Component padre, String titulo, String[] etiquetas, JComponent[] campos) {
        JPanel panel = new JPanel(new GridLayout(etiquetas.length, 2, 6, 6));
        for (int i = 0; i < etiquetas.length; i++) {
            panel.add(new JLabel(etiquetas[i]));
            panel.add(campos[i]);
        }
        return JOptionPane.showConfirmDialog(padre, panel, titulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    /** Muestra un texto largo, no editable y con barra de desplazamiento. */
    public static void texto(Component padre, String titulo, String contenido, Font fuente, Dimension tamano) {
        JTextArea area = new JTextArea(contenido);
        area.setEditable(false);
        area.setFont(fuente);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(tamano);
        JOptionPane.showMessageDialog(padre, scroll, titulo, JOptionPane.INFORMATION_MESSAGE);
    }
}
