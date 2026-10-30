package finanzas.movil.ui;

import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import finanzas.modelo.Categoria;
import finanzas.modelo.Tipo;
import finanzas.util.Formato;

/** Administración de categorías de ingresos y gastos. */
public class CategoriasFragment extends ListaFragment {

    @Override
    protected String ayuda() {
        return "Las categorías aparecen al registrar un movimiento  ·  Mantén pulsado para quitar una";
    }

    @Override
    protected String textoVacio() {
        return "No hay categorías.\nToca + para crear una.";
    }

    @Override
    protected List<Fila> filas() {
        List<Fila> filas = new ArrayList<>();
        for (Categoria c : datos.getCategorias()) {
            filas.add(new Fila(c.nombre(), c.tipo() == Tipo.GASTO ? "Gasto" : "Ingreso", "", c));
        }
        return filas;
    }

    @Override
    protected void alAgregar() {
        Formulario f = new Formulario(requireContext());
        final Spinner tipo = f.opciones("Tipo", Arrays.asList("Gasto", "Ingreso"));
        final EditText nombre = f.texto("Nombre de la categoría", false);
        Dialogos.formulario(requireContext(), "Nueva categoría", f, "Añadir", () -> {
            String n = Formato.limpiar(nombre.getText().toString());
            if (n.isEmpty()) return "Escribe un nombre para la categoría.";
            Tipo t = tipo.getSelectedItemPosition() == 0 ? Tipo.GASTO : Tipo.INGRESO;
            if (!datos.agregarCategoria(new Categoria(t, n))) return "Esa categoría ya existe.";
            cambiaron();
            return null;
        });
    }

    @Override
    protected void alMantener(Fila fila) {
        final Categoria c = (Categoria) fila.dato;
        long usos = datos.usosDeCategoria(c);
        String mensaje = "¿Quitar la categoría «" + c.nombre() + "»?"
                + (usos > 0 ? "\nTiene " + usos + " movimiento(s) registrado(s); se conservan con su categoría." : "");
        Dialogos.confirmar(requireContext(), "Quitar categoría", mensaje, () -> {
            datos.eliminarCategoria(c);
            cambiaron();
        });
    }
}
