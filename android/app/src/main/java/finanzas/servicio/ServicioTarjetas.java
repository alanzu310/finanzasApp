package finanzas.servicio;

import finanzas.datos.DatosFinanzas;
import finanzas.modelo.Categoria;
import finanzas.modelo.Movimiento;
import finanzas.modelo.Tarjeta;
import finanzas.modelo.Tipo;

import java.time.LocalDate;

/** Operaciones que afectan a la vez a una tarjeta y a los movimientos. */
public final class ServicioTarjetas {
    /** Categoría de gasto con la que se registran los pagos de tarjeta. */
    public static final String CATEGORIA_PAGO = "Pago de tarjeta";

    private ServicioTarjetas() {}

    /**
     * Registra un pago: baja el saldo de la tarjeta y lo anota como GASTO en el mes de la fecha indicada
     * (crea la categoría «Pago de tarjeta» si no existe).
     *
     * @throws IllegalArgumentException si el monto no es mayor que 0 (en ese caso no se cambia nada)
     */
    public static Movimiento registrarPago(DatosFinanzas datos, Tarjeta tarjeta, double monto, LocalDate fecha) {
        tarjeta.registrarPago(monto);   // valida el monto antes de tocar los movimientos
        datos.agregarCategoria(new Categoria(Tipo.GASTO, CATEGORIA_PAGO));
        Movimiento gasto = new Movimiento(fecha, Tipo.GASTO, CATEGORIA_PAGO,
                "Pago tarjeta " + tarjeta.getNombre(), monto);
        datos.agregarMovimiento(gasto);
        return gasto;
    }
}
