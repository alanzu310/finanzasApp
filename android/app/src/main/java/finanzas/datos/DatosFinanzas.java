package finanzas.datos;

import finanzas.modelo.*;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Estado en memoria de la aplicación: movimientos, categorías, metas, tarjetas y estimados.
 * No conoce la interfaz gráfica ni cómo se guardan los datos.
 */
public class DatosFinanzas {
    private static final List<String> GASTOS_BASE =
            Arrays.asList("Comida", "Transporte", "Vivienda", "Servicios", "Salud", "Educación", "Ocio", "Otros");
    private static final List<String> INGRESOS_BASE = Arrays.asList("Sueldo", "Ventas", "Otros");
    private static final Comparator<Categoria> ORDEN_CATEGORIAS =
            Comparator.comparing(Categoria::tipo).thenComparing(c -> c.nombre().toLowerCase());

    private final List<Movimiento> movimientos = new ArrayList<>();
    private final List<Meta> metas = new ArrayList<>();
    private final List<Categoria> categorias = new ArrayList<>();
    private final List<Tarjeta> tarjetas = new ArrayList<>();
    private final Map<YearMonth, Estimado> estimados = new TreeMap<>();

    // ----- Movimientos -----
    public List<Movimiento> getMovimientos() { return Collections.unmodifiableList(movimientos); }
    public void agregarMovimiento(Movimiento m) { movimientos.add(m); }
    public boolean eliminarMovimiento(Movimiento m) { return movimientos.remove(m); }

    /** Suma de todos los movimientos de un tipo. */
    public double total(Tipo tipo) {
        return movimientos.stream().filter(m -> m.tipo() == tipo).mapToDouble(Movimiento::monto).sum();
    }

    // ----- Metas -----
    public List<Meta> getMetas() { return Collections.unmodifiableList(metas); }
    public void agregarMeta(Meta m) { metas.add(m); }
    public boolean eliminarMeta(Meta m) { return metas.remove(m); }

    // ----- Tarjetas -----
    public List<Tarjeta> getTarjetas() { return Collections.unmodifiableList(tarjetas); }
    public void agregarTarjeta(Tarjeta t) { tarjetas.add(t); }
    public boolean eliminarTarjeta(Tarjeta t) { return tarjetas.remove(t); }

    // ----- Estimados -----
    public Optional<Estimado> getEstimado(YearMonth mes) { return Optional.ofNullable(estimados.get(mes)); }
    public Map<YearMonth, Estimado> getEstimados() { return Collections.unmodifiableMap(estimados); }
    public void setEstimado(YearMonth mes, Estimado e) { estimados.put(mes, e); }
    public void quitarEstimado(YearMonth mes) { estimados.remove(mes); }

    // ----- Categorías -----
    /** Todas las categorías, ordenadas por tipo y nombre. */
    public List<Categoria> getCategorias() { return Collections.unmodifiableList(categorias); }

    public List<String> categoriasDe(Tipo tipo) {
        return categorias.stream().filter(c -> c.tipo() == tipo).map(Categoria::nombre).collect(Collectors.toList());
    }

    public boolean existeCategoria(Tipo tipo, String nombre) {
        return categorias.stream().anyMatch(c -> c.tipo() == tipo && c.nombre().equalsIgnoreCase(nombre));
    }

    /** @return false si ya existía (sin distinguir mayúsculas) y no se agregó. */
    public boolean agregarCategoria(Categoria c) {
        if (existeCategoria(c.tipo(), c.nombre())) return false;
        categorias.add(c);
        categorias.sort(ORDEN_CATEGORIAS);
        return true;
    }

    public boolean eliminarCategoria(Categoria c) { return categorias.remove(c); }

    /** Cuántos movimientos usan esta categoría. */
    public long usosDeCategoria(Categoria c) {
        return movimientos.stream()
                .filter(m -> m.tipo() == c.tipo() && m.categoria().equalsIgnoreCase(c.nombre())).count();
    }

    /** Categorías iniciales para el primer arranque. */
    public void cargarCategoriasPorDefecto() {
        GASTOS_BASE.forEach(n -> agregarCategoria(new Categoria(Tipo.GASTO, n)));
        INGRESOS_BASE.forEach(n -> agregarCategoria(new Categoria(Tipo.INGRESO, n)));
    }

    /** Crea las categorías que ya aparecen en los movimientos y no existen todavía. */
    public void registrarCategoriasUsadas() {
        for (Movimiento m : movimientos) agregarCategoria(new Categoria(m.tipo(), m.categoria()));
    }
}
