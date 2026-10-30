package finanzas.vista;

import javax.swing.table.DefaultTableModel;

/** Utilidades para crear tablas. */
public final class Tablas {
    private Tablas() {}

    /** Modelo de tabla cuyas celdas no se pueden editar a mano. */
    public static DefaultTableModel soloLectura(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
    }
}
