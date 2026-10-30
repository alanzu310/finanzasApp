package finanzas.modelo;

import java.util.Objects;

/** Categoría para clasificar movimientos (por ejemplo, GASTO / "Comida"). */
public final class Categoria {
    private final Tipo tipo;
    private final String nombre;

    public Categoria(Tipo tipo, String nombre) { this.tipo = tipo; this.nombre = nombre; }

    public Tipo tipo() { return tipo; }
    public String nombre() { return nombre; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Categoria)) return false;
        Categoria c = (Categoria) o;
        return tipo == c.tipo && Objects.equals(nombre, c.nombre);
    }

    @Override public int hashCode() { return Objects.hash(tipo, nombre); }
}
