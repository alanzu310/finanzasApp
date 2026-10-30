package finanzas.modelo;

/** Valores que se espera recibir y gastar en un mes. */
public final class Estimado {
    private final double ingresos;
    private final double gastos;

    public Estimado(double ingresos, double gastos) { this.ingresos = ingresos; this.gastos = gastos; }

    public double ingresos() { return ingresos; }
    public double gastos() { return gastos; }
}
