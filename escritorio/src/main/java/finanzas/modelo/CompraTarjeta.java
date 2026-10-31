package finanzas.modelo;

import java.time.LocalDate;

/** Una compra hecha con una tarjeta de crédito: cuándo, en qué y por cuánto. */
public record CompraTarjeta(LocalDate fecha, String descripcion, double monto) {}
