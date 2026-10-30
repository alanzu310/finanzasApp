package finanzas.movil.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import finanzas.movil.R;

/** Adaptador genérico: sirve para todas las listas de la app (movimientos, metas, tarjetas, etc.). */
public class FilaAdapter extends RecyclerView.Adapter<FilaAdapter.Celda> {

    public interface Accion {
        void ejecutar(Fila fila);
    }

    private final List<Fila> filas = new ArrayList<>();
    private Accion alTocar;
    private Accion alMantener;

    public void setAlTocar(Accion accion) { this.alTocar = accion; }

    public void setAlMantener(Accion accion) { this.alMantener = accion; }

    public void setFilas(List<Fila> nuevas) {
        filas.clear();
        filas.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Celda onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        View vista = LayoutInflater.from(padre.getContext()).inflate(R.layout.item_fila, padre, false);
        return new Celda(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull Celda celda, int posicion) {
        final Fila fila = filas.get(posicion);
        celda.titulo.setText(fila.titulo);
        celda.subtitulo.setText(fila.subtitulo);
        celda.subtitulo.setVisibility(fila.subtitulo.isEmpty() ? View.GONE : View.VISIBLE);
        celda.derecha.setText(fila.derecha);
        celda.derecha.setTextColor(fila.colorDerecha != 0 ? fila.colorDerecha : celda.colorBase);
        celda.itemView.setOnClickListener(v -> {
            if (alTocar != null) alTocar.ejecutar(fila);
        });
        celda.itemView.setOnLongClickListener(v -> {
            if (alMantener == null) return false;
            alMantener.ejecutar(fila);
            return true;
        });
    }

    @Override
    public int getItemCount() { return filas.size(); }

    static class Celda extends RecyclerView.ViewHolder {
        final TextView titulo;
        final TextView subtitulo;
        final TextView derecha;
        final int colorBase;

        Celda(@NonNull View vista) {
            super(vista);
            titulo = vista.findViewById(R.id.titulo);
            subtitulo = vista.findViewById(R.id.subtitulo);
            derecha = vista.findViewById(R.id.derecha);
            colorBase = derecha.getCurrentTextColor();
        }
    }
}
