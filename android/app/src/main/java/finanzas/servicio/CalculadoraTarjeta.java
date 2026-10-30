package finanzas.servicio;

import finanzas.modelo.Escenario;
import finanzas.modelo.Tarjeta;

import java.util.function.DoubleUnaryOperator;

/** Cálculos financieros de una tarjeta de crédito. Todos los métodos son estáticos y sin efectos secundarios. */
public final class CalculadoraTarjeta {
    /** Tope de la simulación: 50 años. */
    private static final int MAX_MESES = 600;

    private CalculadoraTarjeta() {}

    /** Pago mínimo estimado = interés del mes + % de capital mínimo, sin pasar de la deuda total. */
    public static double pagoMinimo(Tarjeta t, double saldo) {
        double i = t.tasaMensual();
        return Math.min(saldo * (1 + i), saldo * i + saldo * t.getCapitalMinPct() / 100.0);
    }

    public static double pagoMinimo(Tarjeta t) { return pagoMinimo(t, t.getSaldo()); }

    /** Cuota fija que liquida 'saldo' en 'n' meses con tasa mensual 'i' (fórmula de amortización). */
    public static double cuotaFija(double saldo, double i, int n) {
        if (saldo <= 0) return 0;
        if (n <= 0) return saldo;
        if (i == 0) return saldo / n;
        return saldo * i / (1 - Math.pow(1 + i, -n));
    }

    /** Cuota que liquida el saldo actual en el plazo elegido para la tarjeta. */
    public static double cuotaRecomendada(Tarjeta t) {
        return cuotaFija(t.getSaldo(), t.tasaMensual(), t.getPlazo());
    }

    /**
     * Simula mes a mes: interés = saldo × i;  saldo = saldo + interés − pago.
     * 'pagoFn' decide cuánto se paga según el saldo de ese mes.
     */
    public static Escenario simular(String nombre, double saldo, double i, DoubleUnaryOperator pagoFn) {
        double s = saldo, intereses = 0, pagado = 0, primero = 0;
        int meses = 0;
        while (s > 0.005 && meses < MAX_MESES) {
            double interes = s * i;
            double pago = (s <= 1.0) ? s + interes : Math.min(pagoFn.applyAsDouble(s), s + interes);
            if (meses == 0) primero = pago;
            s = s + interes - pago;
            intereses += interes;
            pagado += pago;
            meses++;
        }
        return new Escenario(nombre, primero, meses, intereses, pagado, s > 0.005);
    }

    public static Escenario escenarioMinimo(Tarjeta t) {
        return simular("Solo el mínimo cada mes", t.getSaldo(), t.tasaMensual(), s -> pagoMinimo(t, s));
    }

    public static Escenario escenarioRecomendado(Tarjeta t) {
        double cuota = cuotaRecomendada(t);
        return simular("Cuota recomendada (" + t.getPlazo() + " meses)", t.getSaldo(), t.tasaMensual(), s -> cuota);
    }

    /** Pagar todo el estado de cuenta antes del vencimiento: no genera intereses. */
    public static Escenario escenarioTotal(Tarjeta t) {
        return new Escenario("Pagar el total (sin intereses)", t.getSaldo(), t.getSaldo() > 0 ? 1 : 0, 0,
                t.getSaldo(), false);
    }
}
