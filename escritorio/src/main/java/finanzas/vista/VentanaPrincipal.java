package finanzas.vista;

import finanzas.datos.AlmacenCsv;
import finanzas.datos.DatosFinanzas;

import javax.swing.*;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal: arma las pestañas y coordina el guardado y la actualización de todos los paneles.
 */
public class VentanaPrincipal extends JFrame {
    private final DatosFinanzas datos;
    private final AlmacenCsv almacen;
    private final List<PanelBase> paneles = new ArrayList<>();

    public VentanaPrincipal(DatosFinanzas datos, AlmacenCsv almacen) {
        super("Mis Finanzas");
        this.datos = datos;
        this.almacen = almacen;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(980, 600));
        setLocationRelativeTo(null);

        Runnable alCambiarDatos = this::guardarYRefrescar;
        JTabbedPane pestanas = new JTabbedPane();
        agregarPestana(pestanas, "Movimientos", new PanelMovimientos(datos, alCambiarDatos));
        agregarPestana(pestanas, "Mes y estimados", new PanelMes(datos, alCambiarDatos));
        agregarPestana(pestanas, "Resumen", new PanelResumen(datos, alCambiarDatos));
        agregarPestana(pestanas, "Metas de ahorro", new PanelMetas(datos, alCambiarDatos));
        agregarPestana(pestanas, "Tarjetas de crédito", new PanelTarjetas(datos, alCambiarDatos));
        agregarPestana(pestanas, "Categorías", new PanelCategorias(datos, alCambiarDatos));
        add(pestanas);

        refrescarTodo();
    }

    private void agregarPestana(JTabbedPane pestanas, String titulo, PanelBase panel) {
        pestanas.addTab(titulo, panel);
        paneles.add(panel);
    }

    private void refrescarTodo() {
        paneles.forEach(PanelBase::refrescar);
    }

    /** Se ejecuta cada vez que un panel modifica los datos. */
    private void guardarYRefrescar() {
        try {
            almacen.guardar(datos);
        } catch (IOException e) {
            Dialogos.aviso(this, "No se pudo guardar: " + e.getMessage());
        }
        refrescarTodo();
    }
}
