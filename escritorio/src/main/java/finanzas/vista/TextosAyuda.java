package finanzas.vista;

/** Textos largos de ayuda mostrados en la interfaz. */
final class TextosAyuda {
    private TextosAyuda() {}

    static final String TARJETAS = """
            CÓMO FUNCIONA UNA TARJETA DE CRÉDITO

            • Cupo: el máximo que el banco te presta. Lo que gastas lo reduce; lo que pagas lo repone.

            • Corte y vencimiento: cada mes el banco suma tus compras hasta la fecha de corte y emite el
              estado de cuenta. Tienes hasta la fecha de pago para pagarlo.

            • Pago para no generar intereses (pago total): si pagas el total del estado de cuenta antes del
              vencimiento, en general no se cobran intereses por las compras corrientes.

            • Pago mínimo: es lo menos que debes pagar para no caer en mora. Cubre los intereses y una
              pequeña parte del capital (y cargos). Lo que no pagas sigue generando intereses.

            • Interés: el banco publica una tasa anual, pero la cobra cada mes (aprox. tasa anual / 12)
              sobre el saldo pendiente. Como el interés se suma a la deuda, lo no pagado genera más interés
              (interés compuesto).

            • Mora: si no pagas ni el mínimo, se cobran intereses por mora y cargos, y se afecta tu
              historial crediticio.

            • Qué calcula esta pestaña:
                - Mínimo (estimado) = interés del mes + % de capital mínimo × saldo.
                - Recomendado = cuota fija que liquida el saldo en el plazo que elijas
                  (cuota = saldo × i / (1 − (1 + i)^−n), con i = tasa mensual y n = meses).
                - Total = saldo del estado de cuenta, sin intereses.

            • Importante: cada banco calcula el mínimo de forma distinta y puede cobrar comisiones o seguros.
              Usa estos valores como guía y confirma el monto exacto en tu estado de cuenta.
            """;
}
