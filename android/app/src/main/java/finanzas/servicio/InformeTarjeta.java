package finanzas.servicio;

import finanzas.modelo.Escenario;
import finanzas.modelo.Tarjeta;

import java.util.Arrays;

/** Genera el texto de análisis de una tarjeta a partir de los cálculos de {@link CalculadoraTarjeta}. */
public final class InformeTarjeta {
    private InformeTarjeta() {}

    public static String generar(Tarjeta t) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("TARJETA: %s%n", t.getNombre()));
        sb.append(String.format("Cupo: %.2f    Saldo: %.2f    Disponible: %.2f%n",
                t.getCupo(), t.getSaldo(), t.disponible()));
        double uso = t.usoCupoPct();
        if (t.getCupo() > 0)
            sb.append(String.format("Uso del cupo: %.1f%%  %s%n", uso,
                    uso > 30 ? "(alto: lo ideal es mantenerlo por debajo del 30%)" : "(bien: por debajo del 30%)"));
        sb.append(String.format("Tasa: %.2f%% anual  =  %.3f%% mensual%n", t.getTasaAnual(), t.tasaMensual() * 100));

        if (t.getSaldo() <= 0.005) {
            sb.append("\nEsta tarjeta no tiene saldo pendiente.\n");
            sb.append("Mientras pagues el total del estado de cuenta antes del vencimiento, no generas intereses.\n");
            return sb.toString();
        }

        sb.append(String.format("Interés de un mes sobre el saldo (si no pagas el total): %.2f%n%n",
                t.getSaldo() * t.tasaMensual()));

        sb.append("PAGO PARA EL PRÓXIMO VENCIMIENTO\n");
        sb.append(String.format("  Mínimo (estimado)                    : %10.2f   evita la mora, pero la deuda baja muy lento%n",
                CalculadoraTarjeta.pagoMinimo(t)));
        sb.append(String.format("  Recomendado (liquidar en %3d meses)  : %10.2f   cuota fija que termina la deuda a tiempo%n",
                t.getPlazo(), CalculadoraTarjeta.cuotaRecomendada(t)));
        sb.append(String.format("  Total del estado de cuenta           : %10.2f   no se pagan intereses%n%n", t.getSaldo()));

        Escenario eMin = CalculadoraTarjeta.escenarioMinimo(t);
        Escenario eRec = CalculadoraTarjeta.escenarioRecomendado(t);
        Escenario eTot = CalculadoraTarjeta.escenarioTotal(t);

        sb.append("COMPARACIÓN\n");
        sb.append(String.format("%-34s %12s  %-24s %11s %13s%n",
                "Escenario", "Pago 1er mes", "Tiempo", "Intereses", "Total pagado"));
        for (Escenario e : Arrays.asList(eMin, eRec, eTot)) sb.append(fila(e));

        sb.append("\nCONSEJOS\n");
        sb.append(String.format("• Solo con el mínimo pagarías %.2f en intereses; con la cuota recomendada, %.2f (ahorras %.2f).%n",
                eMin.intereses(), eRec.intereses(), eMin.intereses() - eRec.intereses()));
        if (eMin.truncado() || eMin.meses() > 60)
            sb.append("• Con el pago mínimo la deuda casi no se liquida: evita quedarte en ese pago.\n");
        sb.append("• Lo ideal es pagar el total del estado de cuenta cada mes: así no pagas intereses.\n");
        if (t.getCupo() > 0 && uso > 30)
            sb.append("• Tu uso del cupo supera el 30%: conviene bajar el saldo para cuidar tu historial crediticio.\n");
        if (t.getCapitalMinPct() <= 0)
            sb.append("• Con 0% de capital mínimo, el pago mínimo solo cubre intereses y la deuda nunca baja.\n");
        sb.append("• El pago mínimo real figura en tu estado de cuenta; aquí es una estimación (interés del mes + "
                + t.getCapitalMinPct() + "% del saldo).\n");
        return sb.toString();
    }

    private static String fila(Escenario e) {
        String tiempo;
        if (e.truncado()) tiempo = "más de 50 años";
        else if (e.meses() <= 1) tiempo = "1 mes";
        else tiempo = e.meses() + " meses"
                + (e.meses() >= 12 ? String.format(" (%.1f años)", e.meses() / 12.0) : "");
        return String.format("%-34s %12.2f  %-24s %11.2f %13.2f%n",
                e.nombre(), e.primerPago(), tiempo, e.intereses(), e.totalPagado());
    }
}
