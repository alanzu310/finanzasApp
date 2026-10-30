package finanzas.movil.ui;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import finanzas.movil.R;

/** Pantalla con una lista, un botón «+» y un texto de ayuda. Las subclases solo definen qué mostrar y qué hacer. */
public abstract class ListaFragment extends BaseFragment {
    private final FilaAdapter adapter = new FilaAdapter();
    private TextView vacio;

    protected ListaFragment() {
        super(R.layout.fragment_lista);
    }

    @Override
    protected void crearVista(View raiz) {
        TextView cabecera = raiz.findViewById(R.id.cabecera);
        cabecera.setText(ayuda());
        vacio = raiz.findViewById(R.id.vacio);
        vacio.setText(textoVacio());
        RecyclerView lista = raiz.findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adapter);
        adapter.setAlTocar(this::alTocar);
        adapter.setAlMantener(this::alMantener);
        raiz.findViewById(R.id.fab).setOnClickListener(v -> alAgregar());
    }

    @Override
    protected void refrescar() {
        List<Fila> filas = filas();
        adapter.setFilas(filas);
        vacio.setVisibility(filas.isEmpty() ? View.VISIBLE : View.GONE);
    }

    protected abstract String ayuda();

    protected abstract String textoVacio();

    protected abstract List<Fila> filas();

    protected abstract void alAgregar();

    protected void alTocar(Fila fila) {}

    protected void alMantener(Fila fila) {}
}
