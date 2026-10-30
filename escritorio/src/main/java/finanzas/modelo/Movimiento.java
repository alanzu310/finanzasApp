package finanzas.modelo;

import java.time.LocalDate;

/** Un ingreso o un gasto. Es inmutable: si te equivocas, se elimina y se crea otro. */
public record Movimiento(LocalDate fecha, Tipo tipo, String categoria, String descripcion, double monto) {}
