package finanzas.servicio;

import finanzas.modelo.Movimiento;
import finanzas.modelo.Tipo;

import java.time.YearMonth;
import java.util.*;

/** Propone el estimado de un mes promediando los meses anteriores que tengan movimientos. */
public final class EstimadorHistorico {
    private EstimadorHistorico() {}

    public static final class TotalMes {
        private final YearMonth mes;
        private final double ingresos;
        private final double gastos;

        public TotalMes(YearMonth mes, double ingresos, double gastos) {
            this.mes = mes; this.ingresos = ingresos; this.gastos = gastos;
        }

        public YearMonth mes() { return mes; }
        public double ingresos() { return ingresos; }
        public double gastos() { return gastos; }
    }

    public static final class Sugerencia {
        private final List<TotalMes> usados;
        private final double sumaIngresos;
        private final double sumaGastos;

        public Sugerencia(List<TotalMes> usados, double sumaIngresos, double sumaGastos) {
            this.usados = usados; this.sumaIngresos = sumaIngresos; this.sumaGastos = sumaGastos;
        }

        public List<TotalMes> usados() { return usados; }
        public double sumaIngresos() { return sumaIngresos; }
        public double sumaGastos() { return sumaGastos; }
        public double promedioIngresos() { return sumaIngresos / usados.size(); }
        public double promedioGastos() { return sumaGastos / usados.size(); }

        /** Texto que detalla el cálculo realizado. */
        public String explicacion(YearMonth destino, int mesesSolicitados) {
            int k = usados.size();
            StringBuilder sb = new StringBuilder();
            sb.append("Meses usados (hasta ").append(mesesSolicitados)
              .append(" últimos meses con datos antes de ").append(destino).append("):\n\n");
            for (TotalMes t : usados)
                sb.append(String.format("  %s    ingresos %10.2f    gastos %10.2f%n", t.mes(), t.ingresos(), t.gastos()));
            sb.append(String.format("%nFórmula: promedio = suma de los valores / número de meses (%d)%n", k));
            sb.append(String.format("  Ingresos estimados = %.2f / %d = %.2f%n", sumaIngresos, k, promedioIngresos()));
            sb.append(String.format("  Gastos estimados   = %.2f / %d = %.2f%n", sumaGastos, k, promedioGastos()));
            sb.append("\nNota: los meses sin ningún movimiento no se cuentan.\n");
            sb.append("Los valores se colocaron en los campos. Pulsa «Guardar» para confirmarlos,\n");
            sb.append("o ajústalos antes si prevés algo distinto (por ejemplo, un gasto extraordinario).");
            return sb.toString();
        }
    }

    /** Totales de cada mes anterior a 'antesDe' que tenga al menos un movimiento, de más antiguo a más reciente. */
    public static List<TotalMes> totalesAnteriores(List<Movimiento> movimientos, YearMonth antesDe) {
        TreeMap<YearMonth, double[]> porMes = new TreeMap<>();
        for (Movimiento m : movimientos) {
            YearMonth ym = YearMonth.from(m.fecha());
            if (!ym.isBefore(antesDe)) continue;
            double[] v = porMes.computeIfAbsent(ym, k -> new double[2]);
            v[m.tipo() == Tipo.INGRESO ? 0 : 1] += m.monto();
        }
        List<TotalMes> resultado = new ArrayList<>();
        porMes.forEach((mes, v) -> resultado.add(new TotalMes(mes, v[0], v[1])));
        return resultado;
    }

    /** @return vacío si no hay ningún mes anterior con datos */
    public static Optional<Sugerencia> sugerir(List<Movimiento> movimientos, YearMonth destino, int mesesAPromediar) {
        List<TotalMes> todos = totalesAnteriores(movimientos, destino);
        if (todos.isEmpty()) return Optional.empty();
        List<TotalMes> usados = Collections.unmodifiableList(
                new ArrayList<>(todos.subList(Math.max(0, todos.size() - mesesAPromediar), todos.size())));
        double si = 0, sg = 0;
        for (TotalMes t : usados) { si += t.ingresos(); sg += t.gastos(); }
        return Optional.of(new Sugerencia(usados, si, sg));
    }
}
