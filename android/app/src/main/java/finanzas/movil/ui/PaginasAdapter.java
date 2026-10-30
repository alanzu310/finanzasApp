package finanzas.movil.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/** Decide qué pantalla (fragmento) corresponde a cada pestaña. */
public class PaginasAdapter extends FragmentStateAdapter {

    public PaginasAdapter(@NonNull FragmentActivity actividad) {
        super(actividad);
    }

    @NonNull
    @Override
    public Fragment createFragment(int posicion) {
        switch (posicion) {
            case 0: return new MovimientosFragment();
            case 1: return new MesFragment();
            case 2: return new ResumenFragment();
            case 3: return new MetasFragment();
            case 4: return new TarjetasFragment();
            default: return new CategoriasFragment();
        }
    }

    @Override
    public int getItemCount() { return 6; }
}
