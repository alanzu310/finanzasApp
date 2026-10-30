package finanzas.vista;

import finanzas.datos.DatosFinanzas;
import finanzas.servicio.ResumenFinanciero;

import javax.swing.*;
import java.awt.Font;
import java.util.Map;

/** Totales generales, tasa de ahorro y gastos por categoría. */
public class PanelResumen extends PanelBase {
    private final JTextArea texto = new JTextArea();

    public PanelResumen(DatosFinanzas datos, Runnable alCambiarDatos) {
        super(datos, alCambiarDatos);
        texto.setEditable(false);
        texto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        add(new JScrollPane(texto));
    }

    @Override
    public void refrescar() {
        ResumenFinanciero r = ResumenFinanciero.de(datos);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Ingresos totales : %10.2f%n", r.ingresos()));
        sb.append(String.format("Gastos totales   : %10.2f%n", r.gastos()));
        sb.append(String.format("Balance          : %10.2f%n", r.balance()));
        if (r.tieneTasaAhorro()) sb.append(String.format("Tasa de ahorro   : %9.1f%%%n", r.tasaAhorro()));
        sb.append("\nGastos por categoría\n--------------------\n");
        for (Map.Entry<String, Double> e : r.gastosPorCategoria().entrySet()) {
            int barras = r.gastos() > 0 ? (int) Math.round(e.getValue() / r.gastos() * 30) : 0;
            sb.append(String.format("%-14s %9.2f  %s%n", e.getKey(), e.getValue(), "█".repeat(barras)));
        }
        texto.setText(sb.toString());
    }
}
