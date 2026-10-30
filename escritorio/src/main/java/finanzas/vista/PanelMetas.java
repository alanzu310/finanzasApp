package finanzas.vista;

import finanzas.datos.DatosFinanzas;
import finanzas.modelo.Meta;
import finanzas.util.Formato;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Metas de ahorro: crear, abonar y ver el progreso. */
public class PanelMetas extends PanelBase {
    private final DefaultTableModel modelo = Tablas.soloLectura("Meta", "Objetivo", "Ahorrado", "Progreso");
    private final JTable tabla = new JTable(modelo);

    public PanelMetas(DatosFinanzas datos, Runnable alCambiarDatos) {
        super(datos, alCambiarDatos);

        JTextField nombre = new JTextField(12);
        JTextField objetivo = new JTextField(8);
        JButton crear = new JButton("Crear meta");
        JButton abonar = new JButton("Abonar a la seleccionada");
        JButton eliminar = new JButton("Eliminar");
        crear.addActionListener(e -> crear(nombre, objetivo));
        abonar.addActionListener(e -> abonar());
        eliminar.addActionListener(e -> eliminar());

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Nombre:")); form.add(nombre);
        form.add(new JLabel("Objetivo:")); form.add(objetivo);
        form.add(crear); form.add(abonar); form.add(eliminar);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    private void crear(JTextField nombre, JTextField objetivo) {
        try {
            datos.agregarMeta(new Meta(Formato.limpiar(nombre.getText()), Formato.parsear(objetivo.getText()), 0));
        } catch (Exception ex) {
            Dialogos.aviso(this, "Revisa los datos: el nombre es obligatorio y el objetivo debe ser un número mayor que 0.");
            return;
        }
        nombre.setText("");
        objetivo.setText("");
        alCambiarDatos.run();
    }

    private void abonar() {
        Meta meta = metaSeleccionada();
        if (meta == null) return;
        String texto = Dialogos.pedir(this, "¿Cuánto vas a abonar?");
        if (texto == null) return;
        try {
            meta.abonar(Formato.parsear(texto));
        } catch (Exception ex) {
            Dialogos.aviso(this, "Monto inválido");
            return;
        }
        alCambiarDatos.run();
    }

    private void eliminar() {
        Meta meta = metaSeleccionada();
        if (meta == null) return;
        datos.eliminarMeta(meta);
        alCambiarDatos.run();
    }

    private Meta metaSeleccionada() {
        List<Meta> metas = datos.getMetas();
        int fila = tabla.getSelectedRow();
        return (fila >= 0 && fila < metas.size()) ? metas.get(fila) : null;
    }

    @Override
    public void refrescar() {
        modelo.setRowCount(0);
        for (Meta m : datos.getMetas())
            modelo.addRow(new Object[]{m.getNombre(), Formato.dinero(m.getObjetivo()),
                    Formato.dinero(m.getAhorrado()), String.format("%.0f%%", m.progreso())});
    }
}
