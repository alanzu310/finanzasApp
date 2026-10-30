package finanzas.vista;

import finanzas.datos.DatosFinanzas;
import finanzas.modelo.Movimiento;
import finanzas.modelo.Tipo;
import finanzas.util.Formato;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Registro de ingresos y gastos, con búsqueda por mes y día. */
public class PanelMovimientos extends PanelBase {
    private static final String TODOS = "Todos";

    private final DefaultTableModel modelo = Tablas.soloLectura("Fecha", "Tipo", "Categoría", "Descripción", "Monto");
    private final JTable tabla = new JTable(modelo);
    private final JComboBox<Tipo> tipoCb = new JComboBox<>(Tipo.values());
    private final JComboBox<String> categoriaCb = new JComboBox<>();
    private final JComboBox<String> filtroMes = new JComboBox<>();
    private final JComboBox<String> filtroDia = new JComboBox<>();
    private final JLabel balanceLbl = new JLabel();
    /** Movimientos que se ven ahora en la tabla (fila i de la tabla = visibles.get(i)). */
    private final List<Movimiento> visibles = new ArrayList<>();
    /** true mientras se modifican los combos por código, para que sus listeners no reaccionen. */
    private boolean actualizando = false;

    public PanelMovimientos(DatosFinanzas datos, Runnable alCambiarDatos) {
        super(datos, alCambiarDatos);
        JPanel norte = new JPanel(new GridLayout(2, 1));
        norte.add(crearFormulario());
        norte.add(crearFiltros());
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(crearZonaInferior(), BorderLayout.SOUTH);
    }

    // ---------- Construcción de la interfaz ----------

    private JPanel crearFormulario() {
        JTextField fecha = new JTextField(LocalDate.now().toString(), 8);
        JTextField detalle = new JTextField(12);
        JTextField monto = new JTextField(7);
        JButton agregar = new JButton("Agregar");
        categoriaCb.setPrototypeDisplayValue("Transporte    ");

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(tipoCb);
        form.add(new JLabel("Fecha:")); form.add(fecha);
        form.add(new JLabel("Categoría:")); form.add(categoriaCb);
        form.add(new JLabel("Detalle:")); form.add(detalle);
        form.add(new JLabel("Monto:")); form.add(monto);
        form.add(agregar);

        tipoCb.addActionListener(e -> recargarCategorias());
        agregar.addActionListener(e -> agregarMovimiento(fecha, detalle, monto));
        return form;
    }

    private JPanel crearFiltros() {
        filtroMes.addItem(TODOS);
        filtroDia.addItem(TODOS);
        for (int d = 1; d <= 31; d++) filtroDia.addItem(String.valueOf(d));
        filtroMes.addActionListener(e -> { if (!actualizando) refrescarTabla(); });
        filtroDia.addActionListener(e -> { if (!actualizando) refrescarTabla(); });

        JButton quitar = new JButton("Quitar filtros");
        quitar.addActionListener(e -> {
            actualizando = true;
            filtroMes.setSelectedItem(TODOS);
            filtroDia.setSelectedItem(TODOS);
            actualizando = false;
            refrescarTabla();
        });

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Buscar  →  Mes:")); filtros.add(filtroMes);
        filtros.add(new JLabel("Día:")); filtros.add(filtroDia);
        filtros.add(quitar);
        return filtros;
    }

    private JPanel crearZonaInferior() {
        JButton eliminar = new JButton("Eliminar seleccionado");
        eliminar.addActionListener(e -> eliminarSeleccionado());
        balanceLbl.setFont(balanceLbl.getFont().deriveFont(Font.BOLD, 14f));
        JPanel abajo = new JPanel(new BorderLayout());
        abajo.add(eliminar, BorderLayout.WEST);
        abajo.add(balanceLbl, BorderLayout.EAST);
        return abajo;
    }

    // ---------- Acciones ----------

    private void agregarMovimiento(JTextField fecha, JTextField detalle, JTextField monto) {
        String categoria = (String) categoriaCb.getSelectedItem();
        if (categoria == null) {
            Dialogos.aviso(this, "Primero añade una categoría de este tipo en la pestaña «Categorías».");
            return;
        }
        Movimiento nuevo;
        try {
            double valor = Formato.parsear(monto.getText());
            if (valor <= 0) throw new IllegalArgumentException("El monto debe ser mayor que 0");
            nuevo = new Movimiento(LocalDate.parse(fecha.getText().trim()), (Tipo) tipoCb.getSelectedItem(),
                    categoria, Formato.limpiar(detalle.getText()), valor);
        } catch (Exception ex) {
            Dialogos.aviso(this, "Datos inválidos: " + ex.getMessage() + "\n(Fecha en formato AAAA-MM-DD)");
            return;
        }
        datos.agregarMovimiento(nuevo);
        detalle.setText("");
        monto.setText("");
        alCambiarDatos.run();
    }

    private void eliminarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0 && fila < visibles.size()) {
            datos.eliminarMovimiento(visibles.get(fila));
            alCambiarDatos.run();
        }
    }

    // ---------- Actualización de la vista ----------

    @Override
    public void refrescar() {
        recargarCategorias();
        actualizarFiltroMeses();
        refrescarTabla();
    }

    private void recargarCategorias() {
        Object seleccion = categoriaCb.getSelectedItem();
        categoriaCb.removeAllItems();
        for (String nombre : datos.categoriasDe((Tipo) tipoCb.getSelectedItem())) categoriaCb.addItem(nombre);
        if (seleccion != null) categoriaCb.setSelectedItem(seleccion);
    }

    private void actualizarFiltroMeses() {
        actualizando = true;
        Object seleccion = filtroMes.getSelectedItem();
        filtroMes.removeAllItems();
        filtroMes.addItem(TODOS);
        datos.getMovimientos().stream().map(m -> YearMonth.from(m.fecha())).distinct()
                .sorted(Comparator.reverseOrder()).forEach(ym -> filtroMes.addItem(ym.toString()));
        if (seleccion != null) filtroMes.setSelectedItem(seleccion);
        actualizando = false;
    }

    private void refrescarTabla() {
        String mesSel = (String) filtroMes.getSelectedItem();
        String diaSel = (String) filtroDia.getSelectedItem();
        boolean porMes = mesSel != null && !mesSel.equals(TODOS);
        boolean porDia = diaSel != null && !diaSel.equals(TODOS);

        visibles.clear();
        for (Movimiento m : datos.getMovimientos()) {
            if (porMes && !YearMonth.from(m.fecha()).toString().equals(mesSel)) continue;
            if (porDia && m.fecha().getDayOfMonth() != Integer.parseInt(diaSel)) continue;
            visibles.add(m);
        }
        visibles.sort(Comparator.comparing(Movimiento::fecha).reversed());

        modelo.setRowCount(0);
        double ingresos = 0, gastos = 0;
        for (Movimiento m : visibles) {
            modelo.addRow(new Object[]{m.fecha(), m.tipo(), m.categoria(), m.descripcion(), Formato.dinero(m.monto())});
            if (m.tipo() == Tipo.INGRESO) ingresos += m.monto(); else gastos += m.monto();
        }
        double balance = ingresos - gastos;
        balanceLbl.setText(String.format("%sIngresos: %.2f    Gastos: %.2f    Balance: %.2f",
                (porMes || porDia) ? "(filtro activo)  " : "", ingresos, gastos, balance));
        balanceLbl.setForeground(balance >= 0 ? new Color(0, 130, 0) : Color.RED);
    }
}
