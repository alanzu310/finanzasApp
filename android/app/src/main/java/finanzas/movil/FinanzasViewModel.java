package finanzas.movil;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.time.YearMonth;

import finanzas.datos.AlmacenCsv;
import finanzas.datos.DatosFinanzas;

/**
 * Guarda los datos mientras la app está abierta (sobrevive a rotaciones de pantalla) y los persiste en CSV.
 * Las pantallas observan {@link #getVersion()} y se redibujan cada vez que cambian los datos.
 */
public class FinanzasViewModel extends AndroidViewModel {
    private final DatosFinanzas datos = new DatosFinanzas();
    private final AlmacenCsv almacen;
    private final MutableLiveData<Integer> version = new MutableLiveData<>(0);
    private YearMonth mesVista = YearMonth.now();
    private String aviso;
    private boolean errorGrave;

    public FinanzasViewModel(@NonNull Application app) {
        super(app);
        // Almacenamiento interno de la app: privado y se respalda con el Auto Backup de Android
        almacen = new AlmacenCsv(app.getFilesDir().toPath().resolve("finanzas"));
        try {
            int danadas = almacen.cargar(datos);
            if (danadas > 0) {
                aviso = "Se ignoraron " + danadas + " línea(s) dañada(s) al cargar los datos. "
                        + "Se guardó una copia del archivo original con extensión .bak.";
            }
        } catch (IOException e) {
            errorGrave = true;
            aviso = "No se pudieron leer los datos: " + e.getMessage()
                    + "\nLa aplicación se cerrará para no sobrescribir tu información.";
        }
    }

    public DatosFinanzas getDatos() { return datos; }

    public LiveData<Integer> getVersion() { return version; }

    public YearMonth getMesVista() { return mesVista; }

    public void setMesVista(YearMonth mes) { this.mesVista = mes; }

    /** Devuelve el aviso inicial (si lo hay) una sola vez. */
    public String tomarAviso() {
        String a = aviso;
        aviso = null;
        return a;
    }

    public boolean esErrorGrave() { return errorGrave; }

    /** Se llama cada vez que una pantalla modifica los datos: guarda y avisa a todas las pantallas. */
    public void cambiaron() {
        if (!errorGrave) {
            try {
                almacen.guardar(datos);
            } catch (IOException e) {
                aviso = "No se pudo guardar: " + e.getMessage();
            }
        }
        Integer actual = version.getValue();
        version.setValue(actual == null ? 1 : actual + 1);
    }
}
