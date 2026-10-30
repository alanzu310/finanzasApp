package finanzas.movil.ui;

import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import finanzas.modelo.Tarjeta;
import finanzas.servicio.CalculadoraTarjeta;
import finanzas.servicio.InformeTarjeta;
import finanzas.servicio.ServicioTarjetas;
import finanzas.util.Formato;

/** Tarjetas de crédito: saldo, pago mínimo y recomendado, y comparación de escenarios. */
public class TarjetasFragment extends ListaFragment {

    @Override
    protected String ayuda() {
        return "Toca una tarjeta para ver su análisis, registrar pagos o compras  ·  Mantén pulsado para eliminarla";
    }

    @Override
    protected String textoVacio() {
        return "Aún no tienes tarjetas.\nToca + para agregar una.";
    }

    @Override
    protected List<Fila> filas() {
        List<Fila> filas = new ArrayList<>();
        for (Tarjeta t : datos.getTarjetas()) {
            filas.add(new Fila(t.getNombre(),
                    "Saldo " + Formato.dinero(t.getSaldo()) + "  ·  Cupo " + Formato.dinero(t.getCupo())
                            + "  ·  " + String.format("%.2f%%", t.getTasaAnual()) + " anual",
                    "Mín. " + Formato.dinero(CalculadoraTarjeta.pagoMinimo(t)), t));
        }
        return filas;
    }

    @Override
    protected void alAgregar() {
        Formulario f = new Formulario(requireContext());
        final EditText nombre = f.texto("Nombre de la tarjeta", false);
        final EditText cupo = f.texto("Cupo", true);
        final EditText tasa = f.texto("Tasa de interés (%)", true);
        final Spinner tipoTasa = f.opciones("La tasa indicada es", Arrays.asList("Anual", "Mensual"));
        final EditText saldo = f.texto("Saldo actual (opcional)", true);
        final EditText capitalMin = f.texto("Capital mínimo mensual (% del saldo)", true);
        capitalMin.setText("3");
        final EditText plazo = f.texto("Plazo recomendado para pagar (meses)", true);
        plazo.setText("6");

        Dialogos.formulario(requireContext(), "Nueva tarjeta", f, "Agregar", () -> {
            Tarjeta nueva;
            try {
                double tasaAnual = Formato.parsear(tasa.getText().toString());
                if (tipoTasa.getSelectedItemPosition() == 1) tasaAnual *= 12;   // se guarda siempre como anual
                String textoSaldo = saldo.getText().toString().trim();
                nueva = new Tarjeta(Formato.limpiar(nombre.getText().toString()),
                        Formato.parsear(cupo.getText().toString()), tasaAnual,
                        textoSaldo.isEmpty() ? 0 : Formato.parsear(textoSaldo),
                        Formato.parsear(capitalMin.getText().toString()),
                        (int) Math.round(Formato.parsear(plazo.getText().toString())));
            } catch (Exception e) {
                return "Revisa los datos: nombre, cupo, tasa y plazo (en meses) deben ser válidos.";
            }
            datos.agregarTarjeta(nueva);
            cambiaron();
            return null;
        });
    }

    @Override
    protected void alTocar(Fila fila) {
        final Tarjeta t = (Tarjeta) fila.dato;
        new AlertDialog.Builder(requireContext())
                .setTitle(t.getNombre())
                .setView(Dialogos.vistaTexto(requireContext(), InformeTarjeta.generar(t), true))
                .setPositiveButton("Registrar pago", (d, w) -> dialogoMonto(t, false))
                .setNeutralButton("Registrar compra", (d, w) -> dialogoMonto(t, true))
                .setNegativeButton("Cerrar", null)
                .show();
    }

    /** Pide un monto y registra una compra, o un pago (que además se anota como gasto del mes). */
    private void dialogoMonto(final Tarjeta t, final boolean esCompra) {
        Formulario f = new Formulario(requireContext());
        final EditText monto = f.texto(esCompra ? "Monto de la compra" : "Monto del pago", true);
        String titulo = (esCompra ? "Registrar compra en " : "Registrar pago en ") + t.getNombre();
        Dialogos.formulario(requireContext(), titulo, f, "Registrar", () -> {
            try {
                double valor = Formato.parsear(monto.getText().toString());
                if (esCompra) t.registrarCompra(valor);
                else ServicioTarjetas.registrarPago(datos, t, valor, LocalDate.now());
            } catch (Exception e) {
                return "Monto inválido: debe ser un número mayor que 0.";
            }
            cambiaron();
            if (!esCompra) {
                Toast.makeText(requireContext(), "Pago registrado como gasto de este mes", Toast.LENGTH_LONG).show();
            }
            return null;
        });
    }

    @Override
    protected void alMantener(Fila fila) {
        final Tarjeta t = (Tarjeta) fila.dato;
        Dialogos.confirmar(requireContext(), "Eliminar tarjeta", "¿Eliminar la tarjeta «" + t.getNombre() + "»?", () -> {
            datos.eliminarTarjeta(t);
            cambiaron();
        });
    }
}
