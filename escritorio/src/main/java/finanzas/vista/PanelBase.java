package finanzas.vista;

import finanzas.datos.DatosFinanzas;

import javax.swing.*;
import java.awt.BorderLayout;

/**
 * Base de todas las pestañas.
 * Cada panel recibe los datos y una acción 'alCambiarDatos' que debe ejecutar cuando modifica algo:
 * esa acción guarda en disco y le pide a todos los paneles que se redibujen.
 */
public abstract class PanelBase extends JPanel {
    protected final DatosFinanzas datos;
    protected final Runnable alCambiarDatos;

    protected PanelBase(DatosFinanzas datos, Runnable alCambiarDatos) {
        super(new BorderLayout(5, 5));
        this.datos = datos;
        this.alCambiarDatos = alCambiarDatos;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    /** Vuelve a mostrar los datos actuales. */
    public abstract void refrescar();
}
