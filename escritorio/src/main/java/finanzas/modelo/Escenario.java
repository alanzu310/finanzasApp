package finanzas.modelo;

/**
 * Resultado de simular el pago de una deuda de tarjeta.
 *
 * @param truncado true si la deuda no se liquidó dentro del límite de la simulación
 */
public record Escenario(String nombre, double primerPago, int meses, double intereses,
                        double totalPagado, boolean truncado) {}
