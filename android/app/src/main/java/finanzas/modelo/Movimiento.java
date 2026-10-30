package finanzas.modelo;

import java.time.LocalDate;
import java.util.Objects;

/** Un ingreso o un gasto. Es inmutable: si te equivocas, se elimina y se crea otro. */
public final class Movimiento {
    private final LocalDate fecha;
    private final Tipo tipo;
    private final String categoria;
    private final String descripcion;
    private final double monto;

    public Movimiento(LocalDate fecha, Tipo tipo, String categoria, String descripcion, double monto) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.monto = monto;
    }

    public LocalDate fecha() { return fecha; }
    public Tipo tipo() { return tipo; }
    public String categoria() { return categoria; }
    public String descripcion() { return descripcion; }
    public double monto() { return monto; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Movimiento)) return false;
        Movimiento m = (Movimiento) o;
        return Double.compare(monto, m.monto) == 0 && Objects.equals(fecha, m.fecha) && tipo == m.tipo
                && Objects.equals(categoria, m.categoria) && Objects.equals(descripcion, m.descripcion);
    }

    @Override public int hashCode() { return Objects.hash(fecha, tipo, categoria, descripcion, monto); }
}
