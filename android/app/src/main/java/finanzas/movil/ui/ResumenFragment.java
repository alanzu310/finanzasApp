package finanzas.movil.ui;

import android.view.View;
import android.widget.TextView;

import java.util.Map;

import finanzas.movil.R;
import finanzas.servicio.ResumenFinanciero;

/** Totales generales, tasa de ahorro y gastos por categoría. */
public class ResumenFragment extends BaseFragment {
    private TextView texto;

    public ResumenFragment() {
        super(R.layout.fragment_resumen);
    }

    @Override
    protected void crearVista(View raiz) {
        texto = raiz.findViewById(R.id.texto);
    }

    @Override
    protected void refrescar() {
        ResumenFinanciero r = ResumenFinanciero.de(datos);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Ingresos : %10.2f%n", r.ingresos()));
        sb.append(String.format("Gastos   : %10.2f%n", r.gastos()));
        sb.append(String.format("Balance  : %10.2f%n", r.balance()));
        if (r.tieneTasaAhorro()) sb.append(String.format("Ahorro   : %9.1f%%%n", r.tasaAhorro()));
        sb.append("\nGastos por categoría\n--------------------\n");
        for (Map.Entry<String, Double> e : r.gastosPorCategoria().entrySet()) {
            int barras = r.gastos() > 0 ? (int) Math.round(e.getValue() / r.gastos() * 20) : 0;
            StringBuilder grafico = new StringBuilder();
            for (int i = 0; i < barras; i++) grafico.append('#');
            sb.append(String.format("%-13s %9.2f %s%n", e.getKey(), e.getValue(), grafico));
        }
        texto.setText(sb.toString());
    }
}
