package finanzas.modelo;

/** Meta de ahorro: un objetivo y lo que se lleva ahorrado. */
public class Meta {
    private final String nombre;
    private final double objetivo;
    private double ahorrado;

    public Meta(String nombre, double objetivo, double ahorrado) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio");
        if (objetivo <= 0) throw new IllegalArgumentException("El objetivo debe ser mayor que 0");
        if (ahorrado < 0) throw new IllegalArgumentException("Lo ahorrado no puede ser negativo");
        this.nombre = nombre;
        this.objetivo = objetivo;
        this.ahorrado = ahorrado;
    }

    public String getNombre() { return nombre; }
    public double getObjetivo() { return objetivo; }
    public double getAhorrado() { return ahorrado; }

    public void abonar(double monto) {
        if (monto <= 0) throw new IllegalArgumentException("El abono debe ser mayor que 0");
        ahorrado += monto;
    }

    /** Porcentaje alcanzado, limitado a 100. */
    public double progreso() {
        return Math.min(100, ahorrado / objetivo * 100);
    }
}
