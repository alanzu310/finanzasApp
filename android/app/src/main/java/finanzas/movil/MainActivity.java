package finanzas.movil;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import finanzas.movil.ui.Dialogos;
import finanzas.movil.ui.PaginasAdapter;

/** Única pantalla: pestañas deslizables (Movimientos, Mes, Resumen, Metas, Tarjetas, Categorías). */
public class MainActivity extends AppCompatActivity {
    private static final String[] TITULOS = {
            "Movimientos", "Mes y estimados", "Resumen", "Metas", "Tarjetas", "Categorías"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FinanzasViewModel vm = new ViewModelProvider(this).get(FinanzasViewModel.class);

        ViewPager2 pager = findViewById(R.id.pager);
        TabLayout tabs = findViewById(R.id.tabs);
        pager.setAdapter(new PaginasAdapter(this));
        new TabLayoutMediator(tabs, pager, (tab, posicion) -> tab.setText(TITULOS[posicion])).attach();

        String aviso = vm.tomarAviso();
        if (aviso != null) {
            Runnable alCerrar = vm.esErrorGrave() ? this::finish : null;
            Dialogos.aviso(this, aviso, alCerrar);
        }
    }
}
