package finanzas.servicio;

import finanzas.modelo.Movimiento;
import finanzas.modelo.Tipo;

import java.time.YearMonth;
import java.util.List;

/** Ingresos y gastos de cada día de un mes. */
public final class ResumenMensual {
    private final YearMonth mes;
    private final double[] ingresos;
    private final double[] gastos;

    private ResumenMensual(YearMonth mes) {
        this.mes = mes;
        this.ingresos = new double[mes.lengthOfMonth() + 1];   // índice = día del mes (el 0 no se usa)
        this.gastos = new double[mes.lengthOfMonth() + 1];
    }

    public static ResumenMensual de(List<Movimiento> movimientos, YearMonth mes) {
        ResumenMensual r = new ResumenMensual(mes);
        for (Movimiento m : movimientos) {
            if (!YearMonth.from(m.fecha()).equals(mes)) continue;
            int dia = m.fecha().getDayOfMonth();
            if (m.tipo() == Tipo.INGRESO) r.ingresos[dia] += m.monto(); else r.gastos[dia] += m.monto();
        }
        return r;
    }

    public YearMonth getMes() { return mes; }
    public int dias() { return mes.lengthOfMonth(); }
    public double ingresosDia(int dia) { return ingresos[dia]; }
    public double gastosDia(int dia) { return gastos[dia]; }

    public double totalIngresos() { double s = 0; for (double v : ingresos) s += v; return s; }
    public double totalGastos() { double s = 0; for (double v : gastos) s += v; return s; }
}
