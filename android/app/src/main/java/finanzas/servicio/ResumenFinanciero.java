package finanzas.servicio;

import finanzas.datos.DatosFinanzas;
import finanzas.modelo.Movimiento;
import finanzas.modelo.Tipo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Totales globales y gastos por categoría (de mayor a menor). */
public final class ResumenFinanciero {
    private final double ingresos;
    private final double gastos;
    private final Map<String, Double> gastosPorCategoria;

    private ResumenFinanciero(double ingresos, double gastos, Map<String, Double> gastosPorCategoria) {
        this.ingresos = ingresos;
        this.gastos = gastos;
        this.gastosPorCategoria = Collections.unmodifiableMap(gastosPorCategoria);
    }

    public static ResumenFinanciero de(DatosFinanzas datos) {
        Map<String, Double> porCategoria = datos.getMovimientos().stream()
                .filter(m -> m.tipo() == Tipo.GASTO)
                .collect(Collectors.groupingBy(Movimiento::categoria, Collectors.summingDouble(Movimiento::monto)));
        Map<String, Double> ordenado = new LinkedHashMap<>();
        porCategoria.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> ordenado.put(e.getKey(), e.getValue()));
        return new ResumenFinanciero(datos.total(Tipo.INGRESO), datos.total(Tipo.GASTO), ordenado);
    }

    public double ingresos() { return ingresos; }
    public double gastos() { return gastos; }
    public Map<String, Double> gastosPorCategoria() { return gastosPorCategoria; }
    public double balance() { return ingresos - gastos; }
    public boolean tieneTasaAhorro() { return ingresos > 0; }
    public double tasaAhorro() { return balance() / ingresos * 100; }
}
