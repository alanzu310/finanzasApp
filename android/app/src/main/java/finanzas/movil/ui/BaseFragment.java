package finanzas.movil.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import finanzas.datos.DatosFinanzas;
import finanzas.movil.FinanzasViewModel;

/**
 * Base de todas las pantallas. Obtiene los datos del ViewModel compartido y llama a
 * {@link #refrescar()} cada vez que los datos cambian.
 */
public abstract class BaseFragment extends Fragment {
    protected FinanzasViewModel vm;
    protected DatosFinanzas datos;

    protected BaseFragment(@LayoutRes int layout) {
        super(layout);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estado) {
        super.onViewCreated(vista, estado);
        vm = new ViewModelProvider(requireActivity()).get(FinanzasViewModel.class);
        datos = vm.getDatos();
        crearVista(vista);
        vm.getVersion().observe(getViewLifecycleOwner(), version -> refrescar());
    }

    /** Busca las vistas del diseño y configura sus eventos. Se llama una vez. */
    protected abstract void crearVista(View raiz);

    /** Muestra los datos actuales. Se llama al abrir la pantalla y después de cada cambio. */
    protected abstract void refrescar();

    /** Avisar que se modificaron los datos: se guardan y todas las pantallas se actualizan. */
    protected void cambiaron() {
        vm.cambiaron();
    }
}
