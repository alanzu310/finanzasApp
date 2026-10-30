package finanzas.modelo;

/** Tarjeta de crédito: sus condiciones (cupo, tasa) y su saldo pendiente. */
public class Tarjeta {
    private final String nombre;
    private final double cupo;
    private final double tasaAnual;       // porcentaje, por ejemplo 16.77
    private double saldo;
    private final double capitalMinPct;   // % del saldo que se amortiza como mínimo cada mes
    private final int plazo;              // meses en los que se quiere liquidar la deuda

    public Tarjeta(String nombre, double cupo, double tasaAnual, double saldo, double capitalMinPct, int plazo) {
        if (nombre == null || nombre.trim().isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio");
        if (cupo < 0 || tasaAnual < 0 || saldo < 0) throw new IllegalArgumentException("Cupo, tasa y saldo no pueden ser negativos");
        if (capitalMinPct < 0 || capitalMinPct > 100) throw new IllegalArgumentException("El capital mínimo debe estar entre 0 y 100");
        if (plazo < 1) throw new IllegalArgumentException("El plazo debe ser de al menos 1 mes");
        this.nombre = nombre;
        this.cupo = cupo;
        this.tasaAnual = tasaAnual;
        this.saldo = saldo;
        this.capitalMinPct = capitalMinPct;
        this.plazo = plazo;
    }

    public String getNombre() { return nombre; }
    public double getCupo() { return cupo; }
    public double getTasaAnual() { return tasaAnual; }
    public double getSaldo() { return saldo; }
    public double getCapitalMinPct() { return capitalMinPct; }
    public int getPlazo() { return plazo; }

    /** Tasa mensual como fracción (tasa anual / 12). */
    public double tasaMensual() { return tasaAnual / 100.0 / 12.0; }

    public double disponible() { return Math.max(0, cupo - saldo); }

    /** Porcentaje del cupo que está en uso. */
    public double usoCupoPct() { return cupo > 0 ? saldo / cupo * 100 : 0; }

    public void registrarCompra(double monto) {
        if (monto <= 0) throw new IllegalArgumentException("El monto debe ser mayor que 0");
        saldo += monto;
    }

    public void registrarPago(double monto) {
        if (monto <= 0) throw new IllegalArgumentException("El monto debe ser mayor que 0");
        saldo = Math.max(0, saldo - monto);
    }
}
