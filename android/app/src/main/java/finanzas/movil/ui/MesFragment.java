package finanzas.movil.ui;

import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import finanzas.modelo.Estimado;
import finanzas.movil.R;
import finanzas.servicio.EstimadorHistorico;
import finanzas.servicio.ResumenMensual;
import finanzas.util.Formato;

/** Un mes: estimados, avance real vs. estimado y detalle día a día. */
public class MesFragment extends BaseFragment {
    private final FilaAdapter adapter = new FilaAdapter();
    private TextView titulo;
    private TextView textoIngresos;
    private TextView textoGastos;
    private TextView balanceMes;
    private TextView proyeccion;
    private ProgressBar barraIngresos;
    private ProgressBar barraGastos;

    public MesFragment() {
        super(R.layout.fragment_mes);
    }

    @Override
    protected void crearVista(View raiz) {
        titulo = raiz.findViewById(R.id.titulo_mes);
        textoIngresos = raiz.findViewById(R.id.texto_ingresos);
        textoGastos = raiz.findViewById(R.id.texto_gastos);
        balanceMes = raiz.findViewById(R.id.balance_mes);
        proyeccion = raiz.findViewById(R.id.proyeccion);
        barraIngresos = raiz.findViewById(R.id.barra_ingresos);
        barraGastos = raiz.findViewById(R.id.barra_gastos);

        raiz.findViewById(R.id.anterior).setOnClickListener(v -> cambiarMes(vm.getMesVista().minusMonths(1)));
        raiz.findViewById(R.id.siguiente).setOnClickListener(v -> cambiarMes(vm.getMesVista().plusMonths(1)));
        raiz.findViewById(R.id.btn_actual).setOnClickListener(v -> cambiarMes(YearMonth.now()));
        raiz.findViewById(R.id.btn_estimado).setOnClickListener(v -> dialogoEstimado());

        RecyclerView lista = raiz.findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adapter);
    }

    private void cambiarMes(YearMonth mes) {
        vm.setMesVista(mes);
        refrescar();
    }

    @Override
    protected void refrescar() {
        YearMonth mes = vm.getMesVista();
        String nombreMes = mes.getMonth().getDisplayName(TextStyle.FULL, Formato.ES);
        titulo.setText(Character.toUpperCase(nombreMes.charAt(0)) + nombreMes.substring(1) + " " + mes.getYear());

        Optional<Estimado> estimado = datos.getEstimado(mes);
        double estIng = estimado.isPresent() ? estimado.get().ingresos() : 0;
        double estGas = estimado.isPresent() ? estimado.get().gastos() : 0;
        ResumenMensual resumen = ResumenMensual.de(datos.getMovimientos(), mes);
        int hoy = mes.equals(YearMonth.now()) ? LocalDate.now().getDayOfMonth() : -1;

        List<Fila> filas = new ArrayList<>();
        double acIng = 0, acGas = 0;
        for (int d = 1; d <= resumen.dias(); d++) {
            double ing = resumen.ingresosDia(d);
            double gas = resumen.gastosDia(d);
            acIng += ing;
            acGas += gas;
            String dia = String.format("%02d %s", d, mes.atDay(d).getDayOfWeek().getDisplayName(TextStyle.SHORT, Formato.ES));
            if (d == hoy) dia += "   (hoy)";
            String detalle = "Ingresos " + (ing == 0 ? "-" : Formato.dinero(ing))
                    + "   ·   Gastos " + (gas == 0 ? "-" : Formato.dinero(gas))
                    + "\nAcumulado: ingresos " + Formato.dinero(acIng) + "   ·   gastos " + Formato.dinero(acGas);
            String derecha = "";
            int color = 0;
            if (estimado.isPresent()) {
                double disponible = estGas - acGas;
                derecha = Formato.dinero(disponible);
                color = disponible < 0 ? Colores.ROJO : 0;
            }
            filas.add(new Fila(dia, detalle, derecha, color, null));
        }
        adapter.setFilas(filas);

        actualizarBarra(textoIngresos, barraIngresos, "Ingresos", acIng, estIng);
        actualizarBarra(textoGastos, barraGastos, "Gastos", acGas, estGas);

        String balance = "Balance real del mes: " + Formato.dinero(acIng - acGas);
        if (estimado.isPresent()) balance += "   |   estimado: " + Formato.dinero(estIng - estGas);
        balanceMes.setText(balance);
        proyeccion.setText(textoProyeccion(mes, acGas, estGas, resumen.dias(), estimado.isPresent()));
    }

    private String textoProyeccion(YearMonth mes, double gastoReal, double gastoEstimado, int diasDelMes, boolean hayEstimado) {
        if (!mes.equals(YearMonth.now())) return hayEstimado ? " " : "Sin estimado guardado para este mes.";
        double ritmo = gastoReal / LocalDate.now().getDayOfMonth();
        String t = String.format("Ritmo de gasto: %.2f por día. Al ritmo actual terminarías el mes con %.2f en gastos",
                ritmo, ritmo * diasDelMes);
        return t + (gastoEstimado > 0 ? String.format(" (estimado: %.2f).", gastoEstimado) : ".");
    }

    private void actualizarBarra(TextView texto, ProgressBar barra, String nombre, double real, double estimado) {
        if (estimado > 0) {
            int pct = (int) Math.round(real / estimado * 100);
            barra.setProgress(Math.min(100, pct));
            texto.setText(String.format("%s: %s de %s estimados (%d%%)", nombre,
                    Formato.dinero(real), Formato.dinero(estimado), pct));
        } else {
            barra.setProgress(0);
            texto.setText(String.format("%s: %s (sin estimado para este mes)", nombre, Formato.dinero(real)));
        }
    }

    private void dialogoEstimado() {
        final YearMonth mes = vm.getMesVista();
        Optional<Estimado> actual = datos.getEstimado(mes);

        Formulario f = new Formulario(requireContext());
        final EditText ingresos = f.texto("Ingresos estimados del mes", true);
        final EditText gastos = f.texto("Gastos estimados del mes", true);
        if (actual.isPresent()) {
            ingresos.setText(String.format("%.2f", actual.get().ingresos()));
            gastos.setText(String.format("%.2f", actual.get().gastos()));
        }
        final Spinner meses = f.opciones("¿No sabes qué estimar? Promediar los últimos (meses)", Arrays.asList("3", "6", "12"));
        meses.setSelection(1);
        final TextView nota = f.nota("");
        f.boton("Sugerir según mi historial", v -> {
            int n = Integer.parseInt((String) meses.getSelectedItem());
            Optional<EstimadorHistorico.Sugerencia> resultado = EstimadorHistorico.sugerir(datos.getMovimientos(), mes, n);
            if (!resultado.isPresent()) {
                nota.setText("Aún no hay meses anteriores con movimientos registrados.");
                return;
            }
            EstimadorHistorico.Sugerencia s = resultado.get();
            ingresos.setText(String.format("%.2f", s.promedioIngresos()));
            gastos.setText(String.format("%.2f", s.promedioGastos()));
            nota.setText(s.explicacion(mes, n));
        });

        Dialogos.formulario(requireContext(), "Estimado de " + titulo.getText(), f, "Guardar", () -> {
            double ing;
            double gas;
            try {
                String ti = ingresos.getText().toString().trim();
                String tg = gastos.getText().toString().trim();
                ing = ti.isEmpty() ? 0 : Formato.parsear(ti);
                gas = tg.isEmpty() ? 0 : Formato.parsear(tg);
            } catch (Exception e) {
                return "Escribe valores numéricos válidos.";
            }
            if (ing < 0 || gas < 0) return "Los valores deben ser 0 o mayores.";
            if (ing == 0 && gas == 0) datos.quitarEstimado(mes);
            else datos.setEstimado(mes, new Estimado(ing, gas));
            cambiaron();
            return null;
        });
    }
}
