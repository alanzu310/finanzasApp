package finanzas.vista;

import finanzas.datos.DatosFinanzas;
import finanzas.modelo.Estimado;
import finanzas.servicio.EstimadorHistorico;
import finanzas.servicio.ResumenMensual;
import finanzas.util.Formato;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Optional;

/** Vista de un mes: estimados, avance real vs. estimado y detalle día a día. */
public class PanelMes extends PanelBase {
    private static final int COL_GASTO_DISPONIBLE = 6;

    private YearMonth mesVista = YearMonth.now();
    private final JLabel mesLbl = new JLabel();
    private final JTextField estIngresos = new JTextField(8);
    private final JTextField estGastos = new JTextField(8);
    private final JComboBox<Integer> mesesPromedioCb = new JComboBox<>(new Integer[]{3, 6, 12});
    private final JProgressBar barraIngresos = new JProgressBar(0, 100);
    private final JProgressBar barraGastos = new JProgressBar(0, 100);
    private final JLabel balanceLbl = new JLabel(" ");
    private final JLabel proyeccionLbl = new JLabel(" ");
    private final DefaultTableModel modeloDias = Tablas.soloLectura("Día", "Ingresos", "Gastos",
            "Ingresos acum.", "Gastos acum.", "Ingreso por recibir", "Gasto disponible");
    private final JTable tablaDias = new JTable(modeloDias);

    public PanelMes(DatosFinanzas datos, Runnable alCambiarDatos) {
        super(datos, alCambiarDatos);
        add(crearZonaSuperior(), BorderLayout.NORTH);
        tablaDias.setDefaultRenderer(Object.class, new RenderizadorDias());
        add(new JScrollPane(tablaDias), BorderLayout.CENTER);
        cargarEstimadoEnCampos();
    }

    // ---------- Construcción de la interfaz ----------

    private JPanel crearZonaSuperior() {
        JButton anterior = new JButton("« Anterior"), siguiente = new JButton("Siguiente »"), actual = new JButton("Mes actual");
        mesLbl.setFont(mesLbl.getFont().deriveFont(Font.BOLD, 17f));
        anterior.addActionListener(e -> cambiarMes(mesVista.minusMonths(1)));
        siguiente.addActionListener(e -> cambiarMes(mesVista.plusMonths(1)));
        actual.addActionListener(e -> cambiarMes(YearMonth.now()));

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila1.add(anterior); fila1.add(mesLbl); fila1.add(siguiente); fila1.add(actual);

        JButton guardar = new JButton("Guardar estimado");
        guardar.addActionListener(e -> guardarEstimado());
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila2.add(new JLabel("Ingresos estimados:")); fila2.add(estIngresos);
        fila2.add(new JLabel("Gastos estimados:")); fila2.add(estGastos);
        fila2.add(guardar);

        mesesPromedioCb.setSelectedItem(6);
        JButton sugerir = new JButton("Sugerir según mi historial");
        sugerir.addActionListener(e -> sugerirEstimado());
        JPanel fila3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila3.add(new JLabel("¿No sabes qué estimar? Promedio de mis últimos")); fila3.add(mesesPromedioCb);
        fila3.add(new JLabel("meses con datos:")); fila3.add(sugerir);

        balanceLbl.setFont(balanceLbl.getFont().deriveFont(Font.BOLD));
        JPanel zona = new JPanel(new GridLayout(0, 1, 0, 4));
        zona.add(fila1); zona.add(fila2); zona.add(fila3);
        zona.add(barraIngresos); zona.add(barraGastos);
        zona.add(balanceLbl); zona.add(proyeccionLbl);
        return zona;
    }

    // ---------- Acciones ----------

    private void cambiarMes(YearMonth nuevo) {
        mesVista = nuevo;
        cargarEstimadoEnCampos();
        refrescar();
    }

    private void cargarEstimadoEnCampos() {
        Optional<Estimado> e = datos.getEstimado(mesVista);
        estIngresos.setText(e.map(x -> String.format("%.2f", x.ingresos())).orElse(""));
        estGastos.setText(e.map(x -> String.format("%.2f", x.gastos())).orElse(""));
    }

    private void guardarEstimado() {
        double ingresos, gastos;
        try {
            ingresos = estIngresos.getText().isBlank() ? 0 : Formato.parsear(estIngresos.getText());
            gastos = estGastos.getText().isBlank() ? 0 : Formato.parsear(estGastos.getText());
            if (ingresos < 0 || gastos < 0) throw new IllegalArgumentException();
        } catch (Exception ex) {
            Dialogos.aviso(this, "Escribe valores numéricos válidos (0 o mayores).");
            return;
        }
        if (ingresos == 0 && gastos == 0) datos.quitarEstimado(mesVista);
        else datos.setEstimado(mesVista, new Estimado(ingresos, gastos));
        alCambiarDatos.run();
    }

    private void sugerirEstimado() {
        int n = (Integer) mesesPromedioCb.getSelectedItem();
        Optional<EstimadorHistorico.Sugerencia> resultado =
                EstimadorHistorico.sugerir(datos.getMovimientos(), mesVista, n);
        if (resultado.isEmpty()) {
            Dialogos.info(this, "Sin historial", "Aún no hay meses anteriores a " + mesVista
                    + " con movimientos registrados.\nRegistra al menos un mes para poder calcular un promedio.");
            return;
        }
        EstimadorHistorico.Sugerencia s = resultado.get();
        estIngresos.setText(String.format("%.2f", s.promedioIngresos()));
        estGastos.setText(String.format("%.2f", s.promedioGastos()));
        Dialogos.texto(this, "Estimado sugerido para " + mesVista, s.explicacion(mesVista, n),
                new Font(Font.MONOSPACED, Font.PLAIN, 13), new Dimension(620, 300));
    }

    // ---------- Actualización de la vista ----------

    @Override
    public void refrescar() {
        String nombreMes = mesVista.getMonth().getDisplayName(TextStyle.FULL, Formato.ES);
        mesLbl.setText(Character.toUpperCase(nombreMes.charAt(0)) + nombreMes.substring(1) + " " + mesVista.getYear());

        Optional<Estimado> estimado = datos.getEstimado(mesVista);
        double estIng = estimado.map(Estimado::ingresos).orElse(0.0);
        double estGas = estimado.map(Estimado::gastos).orElse(0.0);
        ResumenMensual resumen = ResumenMensual.de(datos.getMovimientos(), mesVista);

        modeloDias.setRowCount(0);
        double acIng = 0, acGas = 0;
        for (int d = 1; d <= resumen.dias(); d++) {
            double ing = resumen.ingresosDia(d), gas = resumen.gastosDia(d);
            acIng += ing;
            acGas += gas;
            String dia = String.format("%02d %s", d, mesVista.atDay(d).getDayOfWeek().getDisplayName(TextStyle.SHORT, Formato.ES));
            modeloDias.addRow(new Object[]{dia,
                    ing == 0 ? "" : Formato.dinero(ing),
                    gas == 0 ? "" : Formato.dinero(gas),
                    Formato.dinero(acIng), Formato.dinero(acGas),
                    estimado.isEmpty() ? "—" : Formato.dinero(estIng - acIng),
                    estimado.isEmpty() ? "—" : Formato.dinero(estGas - acGas)});
        }

        actualizarBarra(barraIngresos, "Ingresos", acIng, estIng);
        actualizarBarra(barraGastos, "Gastos", acGas, estGas);

        String balance = "Balance real del mes: " + Formato.dinero(acIng - acGas);
        if (estimado.isPresent()) balance += "      |      Balance estimado: " + Formato.dinero(estIng - estGas);
        balanceLbl.setText(balance);
        proyeccionLbl.setText(textoProyeccion(acGas, estGas, resumen.dias(), estimado.isPresent()));
    }

    private String textoProyeccion(double gastoReal, double gastoEstimado, int diasDelMes, boolean hayEstimado) {
        if (!mesVista.equals(YearMonth.now())) return hayEstimado ? " " : "Sin estimado guardado para este mes.";
        double ritmo = gastoReal / LocalDate.now().getDayOfMonth();
        String t = String.format("Ritmo de gasto: %.2f por día. Al ritmo actual terminarías el mes con %.2f en gastos",
                ritmo, ritmo * diasDelMes);
        return t + (gastoEstimado > 0 ? String.format(" (estimado: %.2f).", gastoEstimado) : ".");
    }

    private void actualizarBarra(JProgressBar barra, String nombre, double real, double estimado) {
        barra.setStringPainted(true);
        if (estimado > 0) {
            int pct = (int) Math.round(real / estimado * 100);
            barra.setValue(Math.min(100, pct));
            barra.setString(String.format("%s: %.2f de %.2f estimados (%d%%)", nombre, real, estimado, pct));
        } else {
            barra.setValue(0);
            barra.setString(String.format("%s: %.2f (sin estimado para este mes)", nombre, real));
        }
    }

    /** Alinea números a la derecha, pinta en rojo el gasto disponible negativo y resalta el día de hoy. */
    private class RenderizadorDias extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            Component c = super.getTableCellRendererComponent(t, valor, seleccionada, foco, fila, columna);
            setHorizontalAlignment(columna == 0 ? LEFT : RIGHT);
            if (!seleccionada) {
                String texto = valor == null ? "" : valor.toString();
                c.setForeground(columna == COL_GASTO_DISPONIBLE && texto.startsWith("-") ? Color.RED : t.getForeground());
                boolean esHoy = mesVista.equals(YearMonth.now()) && fila + 1 == LocalDate.now().getDayOfMonth();
                c.setBackground(esHoy ? new Color(255, 245, 205) : t.getBackground());
            }
            return c;
        }
    }
}
