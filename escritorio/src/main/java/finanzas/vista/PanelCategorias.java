package finanzas.vista;

import finanzas.datos.DatosFinanzas;
import finanzas.modelo.Categoria;
import finanzas.modelo.Tipo;
import finanzas.util.Formato;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Administración de categorías de ingresos y gastos. */
public class PanelCategorias extends PanelBase {
    private final DefaultTableModel modelo = Tablas.soloLectura("Tipo", "Categoría");
    private final JTable tabla = new JTable(modelo);

    public PanelCategorias(DatosFinanzas datos, Runnable alCambiarDatos) {
        super(datos, alCambiarDatos);

        JComboBox<Tipo> tipo = new JComboBox<>(Tipo.values());
        tipo.setSelectedItem(Tipo.GASTO);
        JTextField nombre = new JTextField(14);
        JButton agregar = new JButton("Añadir categoría");
        JButton quitar = new JButton("Quitar seleccionada");
        agregar.addActionListener(e -> agregar((Tipo) tipo.getSelectedItem(), nombre));
        quitar.addActionListener(e -> quitarSeleccionada());

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Tipo:")); form.add(tipo);
        form.add(new JLabel("Nombre:")); form.add(nombre);
        form.add(agregar); form.add(quitar);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(new JLabel("Las categorías añadidas aparecen en la lista al registrar un movimiento."), BorderLayout.SOUTH);
    }

    private void agregar(Tipo tipo, JTextField campoNombre) {
        String nombre = Formato.limpiar(campoNombre.getText());
        if (nombre.isBlank()) {
            Dialogos.aviso(this, "Escribe un nombre para la categoría.");
            return;
        }
        if (!datos.agregarCategoria(new Categoria(tipo, nombre))) {
            Dialogos.aviso(this, "Esa categoría ya existe.");
            return;
        }
        campoNombre.setText("");
        alCambiarDatos.run();
    }

    private void quitarSeleccionada() {
        List<Categoria> categorias = datos.getCategorias();
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= categorias.size()) return;
        Categoria c = categorias.get(fila);
        long usos = datos.usosDeCategoria(c);
        String mensaje = "¿Quitar la categoría «" + c.nombre() + "»?"
                + (usos > 0 ? "\nTiene " + usos + " movimiento(s) registrado(s); se conservan con su categoría." : "");
        if (Dialogos.confirmar(this, "Quitar categoría", mensaje)) {
            datos.eliminarCategoria(c);
            alCambiarDatos.run();
        }
    }

    @Override
    public void refrescar() {
        modelo.setRowCount(0);
        for (Categoria c : datos.getCategorias()) modelo.addRow(new Object[]{c.tipo(), c.nombre()});
    }
}
