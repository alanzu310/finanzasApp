package finanzas.util;

import java.util.Locale;

/** Funciones de apoyo para convertir y mostrar números y textos. */
public final class Formato {
    public static final Locale ES = Locale.forLanguageTag("es");

    private Formato() {}

    /** Dinero con dos decimales (evita mostrar "-0.00"). */
    public static String dinero(double v) {
        return String.format("%.2f", Math.abs(v) < 0.005 ? 0.0 : v);
    }

    /** Convierte texto a número aceptando coma o punto decimal. */
    public static double parsear(String s) {
        return Double.parseDouble(s.trim().replace(',', '.'));
    }

    /** Quita el separador ';' (usado en los archivos CSV) y los espacios sobrantes. */
    public static String limpiar(String s) {
        return s.replace(';', ' ').trim();
    }
}
