package finanzas.movil.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.List;

/** Arma formularios sencillos (etiqueta + campo) para mostrarlos dentro de un cuadro de diálogo. */
public class Formulario {
    private final Context contexto;
    private final LinearLayout raiz;

    public Formulario(Context contexto) {
        this.contexto = contexto;
        raiz = new LinearLayout(contexto);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(20), dp(8), dp(20), 0);
    }

    private int dp(int valor) {
        return Math.round(valor * contexto.getResources().getDisplayMetrics().density);
    }

    private void etiqueta(String texto) {
        TextView t = new TextView(contexto);
        t.setText(texto);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        t.setPadding(0, dp(12), 0, 0);
        raiz.addView(t);
    }

    /** Campo de texto; si 'numerico' es true muestra el teclado numérico con decimales. */
    public EditText texto(String etiqueta, boolean numerico) {
        etiqueta(etiqueta);
        EditText campo = new EditText(contexto);
        campo.setSingleLine(true);
        if (numerico) {
            campo.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else {
            campo.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        }
        raiz.addView(campo);
        return campo;
    }

    /** Lista desplegable. */
    public Spinner opciones(String etiqueta, List<String> items) {
        etiqueta(etiqueta);
        Spinner spinner = new Spinner(contexto);
        spinner.setAdapter(adaptador(contexto, items));
        raiz.addView(spinner);
        return spinner;
    }

    /** Campo de fecha: al tocarlo se abre el selector de fecha. El texto queda como AAAA-MM-DD. */
    public EditText fecha(String etiqueta, LocalDate inicial) {
        etiqueta(etiqueta);
        final EditText campo = new EditText(contexto);
        campo.setFocusable(false);
        campo.setText(inicial.toString());
        campo.setOnClickListener(v -> {
            LocalDate actual;
            try {
                actual = LocalDate.parse(campo.getText().toString());
            } catch (Exception e) {
                actual = LocalDate.now();
            }
            new DatePickerDialog(contexto,
                    (vista, anio, mes, dia) -> campo.setText(LocalDate.of(anio, mes + 1, dia).toString()),
                    actual.getYear(), actual.getMonthValue() - 1, actual.getDayOfMonth()).show();
        });
        raiz.addView(campo);
        return campo;
    }

    /** Texto informativo (por ejemplo, para mostrar un cálculo). */
    public TextView nota(String texto) {
        TextView t = new TextView(contexto);
        t.setText(texto);
        t.setTypeface(Typeface.MONOSPACE);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        t.setPadding(0, dp(12), 0, 0);
        raiz.addView(t);
        return t;
    }

    public Button boton(String texto, View.OnClickListener alTocar) {
        Button b = new Button(contexto);
        b.setText(texto);
        b.setOnClickListener(alTocar);
        raiz.addView(b);
        return b;
    }

    /** Vista lista para pasar a un diálogo (con desplazamiento por si el teclado ocupa espacio). */
    public View vista() {
        ScrollView scroll = new ScrollView(contexto);
        scroll.addView(raiz);
        return scroll;
    }

    public static ArrayAdapter<String> adaptador(Context contexto, List<String> items) {
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(contexto, android.R.layout.simple_spinner_item, items);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adaptador;
    }
}
