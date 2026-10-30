package finanzas.vista;

import finanzas.datos.DatosFinanzas;
import finanzas.modelo.Tarjeta;
import finanzas.servicio.CalculadoraTarjeta;
import finanzas.servicio.InformeTarjeta;
import finanzas.servicio.ServicioTarjetas;
import finanzas.util.Formato;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** Tarjetas de crédito: saldo, pagos mínimo y recomendado, y comparación de escenarios. */
public class PanelTarjetas extends PanelBase {
    private final DefaultTableModel modelo = Tablas.soloLectura("Tarjeta", "Cupo", "Tasa anual", "Saldo",
            "Mínimo (estimado)", "Recomendado");
    private final JTable tabla = new JTable(modelo);
    private final JTextArea analisis = new JTextArea();
    /** true mientras se reconstruye la tabla, para que el listener de selección no reaccione. */
    private boolean actualizando = false;

    public PanelTarjetas(DatosFinanzas datos, Runnable alCambiarDatos) {
        super(datos, alCambiarDatos);

        analisis.setEditable(false);
        analisis.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !actualizando) mostrarAnalisis();
        });

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tabla), new JScrollPane(analisis));
        division.setResizeWeight(0.25);

        add(crearFormulario(), BorderLayout.NORTH);
        add(division, BorderLayout.CENTER);
    }

    // ---------- Construcción de la interfaz ----------

    private JPanel crearFormulario() {
        JTextField nombre = new JTextField(10), cupo = new JTextField(7), tasa = new JTextField(6),
                saldo = new JTextField(7), capMin = new JTextField("3", 4), plazo = new JTextField("6", 3);
        JComboBox<String> tipoTasa = new JComboBox<>(new String[]{"anual", "mensual"});
        JButton agregar = new JButton("Agregar tarjeta");
        agregar.addActionListener(e -> agregarTarjeta(nombre, cupo, tasa, tipoTasa, saldo, capMin, plazo));

        JButton compra = new JButton("Registrar compra"), pago = new JButton("Registrar pago");
        JButton eliminar = new JButton("Eliminar tarjeta"), ayuda = new JButton("¿Cómo funciona una tarjeta?");
        compra.addActionListener(e -> registrarMovimiento(true));
        pago.addActionListener(e -> registrarMovimiento(false));
        pago.setToolTipText("Baja el saldo y se anota como gasto de este mes (categoría «Pago de tarjeta»)");
        eliminar.addActionListener(e -> eliminarSeleccionada());
        ayuda.addActionListener(e -> Dialogos.texto(this, "¿Cómo funciona una tarjeta de crédito?",
                TextosAyuda.TARJETAS, new Font(Font.SANS_SERIF, Font.PLAIN, 13), new Dimension(640, 420)));

        JPanel f1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        f1.add(new JLabel("Nombre:")); f1.add(nombre);
        f1.add(new JLabel("Cupo:")); f1.add(cupo);
        f1.add(new JLabel("Tasa de interés:")); f1.add(tasa); f1.add(new JLabel("%")); f1.add(tipoTasa);
        f1.add(new JLabel("Saldo actual:")); f1.add(saldo);
        JPanel f2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        f2.add(new JLabel("Capital mínimo mensual (% del saldo):")); f2.add(capMin);
        f2.add(new JLabel("Plazo recomendado (meses):")); f2.add(plazo);
        f2.add(agregar);
        JPanel f3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        f3.add(compra); f3.add(pago); f3.add(eliminar); f3.add(ayuda);

        JPanel zona = new JPanel(new GridLayout(3, 1));
        zona.add(f1); zona.add(f2); zona.add(f3);
        return zona;
    }

    // ---------- Acciones ----------

    private void agregarTarjeta(JTextField nombre, JTextField cupo, JTextField tasa, JComboBox<String> tipoTasa,
                                JTextField saldo, JTextField capMin, JTextField plazo) {
        Tarjeta nueva;
        try {
            double tasaAnual = Formato.parsear(tasa.getText());
            if ("mensual".equals(tipoTasa.getSelectedItem())) tasaAnual *= 12;   // se guarda siempre como anual
            nueva = new Tarjeta(Formato.limpiar(nombre.getText()), Formato.parsear(cupo.getText()), tasaAnual,
                    saldo.getText().isBlank() ? 0 : Formato.parsear(saldo.getText()),
                    Formato.parsear(capMin.getText()), Integer.parseInt(plazo.getText().trim()));
        } catch (Exception ex) {
            Dialogos.aviso(this, "Revisa los datos de la tarjeta: nombre, cupo, tasa y plazo (en meses) deben ser válidos.");
            return;
        }
        datos.agregarTarjeta(nueva);
        nombre.setText(""); cupo.setText(""); tasa.setText(""); saldo.setText("");
        alCambiarDatos.run();
    }

    private void registrarMovimiento(boolean esCompra) {
        Tarjeta t = tarjetaSeleccionada();
        if (t == null) { Dialogos.aviso(this, "Selecciona una tarjeta de la tabla."); return; }
        String texto = Dialogos.pedir(this, esCompra ? "Monto de la compra:" : "Monto del pago:");
        if (texto == null) return;
        try {
            double monto = Formato.parsear(texto);
            if (esCompra) t.registrarCompra(monto);
            else ServicioTarjetas.registrarPago(datos, t, monto, LocalDate.now());   // también se anota como gasto del mes
        } catch (Exception ex) {
            Dialogos.aviso(this, "Monto inválido");
            return;
        }
        alCambiarDatos.run();
    }

    private void eliminarSeleccionada() {
        Tarjeta t = tarjetaSeleccionada();
        if (t == null) return;
        datos.eliminarTarjeta(t);
        alCambiarDatos.run();
    }

    private Tarjeta tarjetaSeleccionada() {
        List<Tarjeta> tarjetas = datos.getTarjetas();
        int fila = tabla.getSelectedRow();
        return (fila >= 0 && fila < tarjetas.size()) ? tarjetas.get(fila) : null;
    }

    // ---------- Actualización de la vista ----------

    @Override
    public void refrescar() {
        int seleccion = tabla.getSelectedRow();
        actualizando = true;
        modelo.setRowCount(0);
        for (Tarjeta t : datos.getTarjetas())
            modelo.addRow(new Object[]{t.getNombre(), Formato.dinero(t.getCupo()),
                    String.format("%.2f%%", t.getTasaAnual()), Formato.dinero(t.getSaldo()),
                    Formato.dinero(CalculadoraTarjeta.pagoMinimo(t)),
                    Formato.dinero(CalculadoraTarjeta.cuotaRecomendada(t))});
        if (seleccion >= 0 && seleccion < datos.getTarjetas().size()) tabla.setRowSelectionInterval(seleccion, seleccion);
        actualizando = false;
        mostrarAnalisis();
    }

    private void mostrarAnalisis() {
        Tarjeta t = tarjetaSeleccionada();
        analisis.setText(t != null ? InformeTarjeta.generar(t)
                : "Selecciona una tarjeta de la tabla para ver su análisis:\n"
                  + "pagos mínimo y recomendado, tiempo para liquidar la deuda e intereses.");
        analisis.setCaretPosition(0);
    }
}
