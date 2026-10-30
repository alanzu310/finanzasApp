package finanzas.movil.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

/** Cuadros de diálogo reutilizables. */
public final class Dialogos {

    /** Se ejecuta al pulsar el botón principal. Devuelve un mensaje de error, o null si todo salió bien. */
    public interface Validador {
        String aceptar();
    }

    private Dialogos() {}

    /** Muestra un formulario; si el validador devuelve un error, el diálogo queda abierto y se muestra el mensaje. */
    public static void formulario(final Context contexto, String titulo, Formulario formulario,
                                  String textoOk, final Validador validador) {
        final AlertDialog dialogo = new AlertDialog.Builder(contexto)
                .setTitle(titulo)
                .setView(formulario.vista())
                .setPositiveButton(textoOk, null)
                .setNegativeButton("Cancelar", null)
                .create();
        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String error = validador.aceptar();
            if (error == null) {
                dialogo.dismiss();
            } else {
                Toast.makeText(contexto, error, Toast.LENGTH_LONG).show();
            }
        }));
        dialogo.show();
    }

    public static void aviso(Context contexto, String mensaje, final Runnable alCerrar) {
        new AlertDialog.Builder(contexto)
                .setTitle("Aviso")
                .setMessage(mensaje)
                .setCancelable(alCerrar == null)
                .setPositiveButton("Aceptar", (d, w) -> {
                    if (alCerrar != null) alCerrar.run();
                })
                .show();
    }

    public static void confirmar(Context contexto, String titulo, String mensaje, final Runnable siAcepta) {
        new AlertDialog.Builder(contexto)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Eliminar", (d, w) -> siAcepta.run())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /** Texto largo con desplazamiento. En modo monoespaciado se desplaza también hacia los lados. */
    public static View vistaTexto(Context contexto, String texto, boolean monoespaciado) {
        TextView t = new TextView(contexto);
        t.setText(texto);
        t.setTextIsSelectable(true);
        int p = Math.round(16 * contexto.getResources().getDisplayMetrics().density);
        t.setPadding(p, p, p, p);
        ScrollView vertical = new ScrollView(contexto);
        if (monoespaciado) {
            t.setTypeface(Typeface.MONOSPACE);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            HorizontalScrollView horizontal = new HorizontalScrollView(contexto);
            horizontal.addView(t);
            vertical.addView(horizontal);
        } else {
            vertical.addView(t);
        }
        return vertical;
    }
}
