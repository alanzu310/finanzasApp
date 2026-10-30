package finanzas.modelo;

/** Resultado de simular el pago de una deuda de tarjeta. 'truncado' = no se liquidó dentro del límite. */
public final class Escenario {
    private final String nombre;
    private final double primerPago;
    private final int meses;
    private final double intereses;
    private final double totalPagado;
    private final boolean truncado;

    public Escenario(String nombre, double primerPago, int meses, double intereses,
                     double totalPagado, boolean truncado) {
        this.nombre = nombre; this.primerPago = primerPago; this.meses = meses;
        this.intereses = intereses; this.totalPagado = totalPagado; this.truncado = truncado;
    }

    public String nombre() { return nombre; }
    public double primerPago() { return primerPago; }
    public int meses() { return meses; }
    public double intereses() { return intereses; }
    public double totalPagado() { return totalPagado; }
    public boolean truncado() { return truncado; }
}
