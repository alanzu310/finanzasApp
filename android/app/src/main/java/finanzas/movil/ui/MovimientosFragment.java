package finanzas.movil.ui;

import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import finanzas.modelo.Movimiento;
import finanzas.modelo.Tipo;
import finanzas.movil.R;
import finanzas.util.Formato;

/** Ingresos y gastos: lista, búsqueda por mes y día, y alta de movimientos. */
public class MovimientosFragment extends BaseFragment {
    private static final String TODOS = "Todos";

    private final FilaAdapter adapter = new FilaAdapter();
    private Spinner filtroMes;
    private Spinner filtroDia;
    private TextView balance;
    private View vacio;
    /** true mientras se cambian los spinners por código, para que no reaccionen. */
    private boolean actualizando = false;

    public MovimientosFragment() {
        super(R.layout.fragment_movimientos);
    }

    @Override
    protected void crearVista(View raiz) {
        filtroMes = raiz.findViewById(R.id.filtroMes);
        filtroDia = raiz.findViewById(R.id.filtroDia);
        balance = raiz.findViewById(R.id.balance);
        vacio = raiz.findViewById(R.id.vacio);

        List<String> dias = new ArrayList<>();
        dias.add(TODOS);
        for (int d = 1; d <= 31; d++) dias.add(String.valueOf(d));
        filtroDia.setAdapter(Formulario.adaptador(requireContext(), dias));

        AdapterView.OnItemSelectedListener alElegir = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> padre, View vista, int posicion, long id) {
                if (!actualizando) mostrar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> padre) {}
        };
        filtroMes.setOnItemSelectedListener(alElegir);
        filtroDia.setOnItemSelectedListener(alElegir);

        raiz.findViewById(R.id.quitarFiltros).setOnClickListener(v -> {
            filtroMes.setSelection(0);
            filtroDia.setSelection(0);
            mostrar();
        });

        RecyclerView lista = raiz.findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adapter);
        adapter.setAlMantener(fila -> {
            final Movimiento m = (Movimiento) fila.dato;
            Dialogos.confirmar(requireContext(), "Eliminar movimiento",
                    "¿Eliminar " + m.categoria() + " por " + Formato.dinero(m.monto()) + " del " + m.fecha() + "?",
                    () -> {
                        datos.eliminarMovimiento(m);
                        cambiaron();
                    });
        });
        raiz.findViewById(R.id.fab).setOnClickListener(v -> dialogoAgregar());
    }

    @Override
    protected void refrescar() {
        actualizarMeses();
        mostrar();
    }

    /** Reconstruye la lista de meses con movimientos, conservando el mes elegido. */
    private void actualizarMeses() {
        String seleccionado = (String) filtroMes.getSelectedItem();
        TreeSet<YearMonth> meses = new TreeSet<>(Collections.<YearMonth>reverseOrder());
        for (Movimiento m : datos.getMovimientos()) meses.add(YearMonth.from(m.fecha()));
        List<String> items = new ArrayList<>();
        items.add(TODOS);
        for (YearMonth ym : meses) items.add(ym.toString());

        actualizando = true;
        filtroMes.setAdapter(Formulario.adaptador(requireContext(), items));
        int indice = seleccionado == null ? 0 : items.indexOf(seleccionado);
        filtroMes.setSelection(Math.max(0, indice));
        actualizando = false;
    }

    private void mostrar() {
        String mesSel = (String) filtroMes.getSelectedItem();
        String diaSel = (String) filtroDia.getSelectedItem();
        boolean porMes = mesSel != null && !mesSel.equals(TODOS);
        boolean porDia = diaSel != null && !diaSel.equals(TODOS);

        List<Movimiento> visibles = new ArrayList<>();
        for (Movimiento m : datos.getMovimientos()) {
            if (porMes && !YearMonth.from(m.fecha()).toString().equals(mesSel)) continue;
            if (porDia && m.fecha().getDayOfMonth() != Integer.parseInt(diaSel)) continue;
            visibles.add(m);
        }
        Collections.sort(visibles, (a, b) -> b.fecha().compareTo(a.fecha()));

        List<Fila> filas = new ArrayList<>();
        double ingresos = 0, gastos = 0;
        for (Movimiento m : visibles) {
            boolean esIngreso = m.tipo() == Tipo.INGRESO;
            String titulo = m.categoria() + (m.descripcion().isEmpty() ? "" : "  ·  " + m.descripcion());
            filas.add(new Fila(titulo, m.fecha().toString(),
                    (esIngreso ? "+" : "-") + Formato.dinero(m.monto()),
                    esIngreso ? Colores.VERDE : Colores.ROJO, m));
            if (esIngreso) ingresos += m.monto(); else gastos += m.monto();
        }
        adapter.setFilas(filas);
        vacio.setVisibility(filas.isEmpty() ? View.VISIBLE : View.GONE);

        double saldo = ingresos - gastos;
        balance.setText((porMes || porDia ? "(filtro activo)  " : "")
                + "Ingresos " + Formato.dinero(ingresos) + "   Gastos " + Formato.dinero(gastos)
                + "\nBalance " + Formato.dinero(saldo));
        balance.setTextColor(saldo >= 0 ? Colores.VERDE : Colores.ROJO);
    }

    private void dialogoAgregar() {
        Formulario f = new Formulario(requireContext());
        final Spinner tipo = f.opciones("Tipo", Arrays.asList("Gasto", "Ingreso"));
        final Spinner categoria = f.opciones("Categoría", datos.categoriasDe(Tipo.GASTO));
        final EditText fecha = f.fecha("Fecha", LocalDate.now());
        final EditText detalle = f.texto("Detalle (opcional)", false);
        final EditText monto = f.texto("Monto", true);

        tipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> padre, View vista, int posicion, long id) {
                Tipo t = posicion == 0 ? Tipo.GASTO : Tipo.INGRESO;
                categoria.setAdapter(Formulario.adaptador(requireContext(), datos.categoriasDe(t)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> padre) {}
        });

        Dialogos.formulario(requireContext(), "Nuevo movimiento", f, "Guardar", () -> {
            Object cat = categoria.getSelectedItem();
            if (cat == null) return "Primero añade una categoría de este tipo en la pestaña Categorías.";
            double valor;
            LocalDate dia;
            try {
                valor = Formato.parsear(monto.getText().toString());
                dia = LocalDate.parse(fecha.getText().toString().trim());
            } catch (Exception e) {
                return "Datos inválidos: revisa el monto y la fecha.";
            }
            if (valor <= 0) return "El monto debe ser mayor que 0.";
            Tipo t = tipo.getSelectedItemPosition() == 0 ? Tipo.GASTO : Tipo.INGRESO;
            datos.agregarMovimiento(new Movimiento(dia, t, (String) cat,
                    Formato.limpiar(detalle.getText().toString()), valor));
            cambiaron();
            return null;
        });
    }
}
