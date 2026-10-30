package finanzas;

import finanzas.datos.AlmacenCsv;
import finanzas.datos.DatosFinanzas;
import finanzas.vista.Dialogos;
import finanzas.vista.VentanaPrincipal;

import javax.swing.SwingUtilities;
import java.io.IOException;

/** Punto de entrada: carga los datos y abre la ventana principal. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        // Swing debe crearse en su hilo especial (Event Dispatch Thread)
        SwingUtilities.invokeLater(Main::iniciar);
    }

    private static void iniciar() {
        DatosFinanzas datos = new DatosFinanzas();
        AlmacenCsv almacen = new AlmacenCsv();
        try {
            int dañadas = almacen.cargar(datos);
            if (dañadas > 0)
                Dialogos.aviso(null, "Se ignoraron " + dañadas + " línea(s) dañada(s) al cargar los datos.\n"
                        + "Se guardó una copia del archivo original con extensión .bak en " + almacen.getDirectorio());
        } catch (IOException e) {
            Dialogos.aviso(null, "No se pudieron leer los datos: " + e.getMessage()
                    + "\nLa aplicación se cerrará para no sobrescribir tu información.");
            System.exit(1);
        }
        new VentanaPrincipal(datos, almacen).setVisible(true);
    }
}
