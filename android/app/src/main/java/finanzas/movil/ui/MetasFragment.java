package finanzas.movil.ui;

import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import finanzas.modelo.Meta;
import finanzas.util.Formato;

/** Metas de ahorro: crear, abonar y ver el progreso. */
public class MetasFragment extends ListaFragment {

    @Override
    protected String ayuda() {
        return "Toca una meta para abonar  ·  Mantén pulsado para eliminarla";
    }

    @Override
    protected String textoVacio() {
        return "Aún no tienes metas de ahorro.\nToca + para crear una.";
    }

    @Override
    protected List<Fila> filas() {
        List<Fila> filas = new ArrayList<>();
        for (Meta m : datos.getMetas()) {
            filas.add(new Fila(m.getNombre(),
                    "Ahorrado " + Formato.dinero(m.getAhorrado()) + " de " + Formato.dinero(m.getObjetivo()),
                    String.format("%.0f%%", m.progreso()), m));
        }
        return filas;
    }

    @Override
    protected void alAgregar() {
        Formulario f = new Formulario(requireContext());
        final EditText nombre = f.texto("Nombre de la meta", false);
        final EditText objetivo = f.texto("Objetivo", true);
        Dialogos.formulario(requireContext(), "Nueva meta", f, "Crear", () -> {
            try {
                datos.agregarMeta(new Meta(Formato.limpiar(nombre.getText().toString()),
                        Formato.parsear(objetivo.getText().toString()), 0));
            } catch (Exception e) {
                return "Revisa los datos: el nombre es obligatorio y el objetivo debe ser mayor que 0.";
            }
            cambiaron();
            return null;
        });
    }

    @Override
    protected void alTocar(Fila fila) {
        final Meta meta = (Meta) fila.dato;
        Formulario f = new Formulario(requireContext());
        final EditText monto = f.texto("¿Cuánto vas a abonar?", true);
        Dialogos.formulario(requireContext(), "Abonar a " + meta.getNombre(), f, "Abonar", () -> {
            try {
                meta.abonar(Formato.parsear(monto.getText().toString()));
            } catch (Exception e) {
                return "Monto inválido: debe ser un número mayor que 0.";
            }
            cambiaron();
            return null;
        });
    }

    @Override
    protected void alMantener(Fila fila) {
        final Meta meta = (Meta) fila.dato;
        Dialogos.confirmar(requireContext(), "Eliminar meta", "¿Eliminar la meta «" + meta.getNombre() + "»?", () -> {
            datos.eliminarMeta(meta);
            cambiaron();
        });
    }
}
