package finanzas.datos;

import finanzas.modelo.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Guarda y lee los datos en archivos CSV (separador ';') dentro de una carpeta.
 * Por defecto usa  ~/.finanzas/
 */
public class AlmacenCsv {
    private static final String SEP = ";";

    private final Path dir;

    public AlmacenCsv() {
        this(Paths.get(System.getProperty("user.home"), ".finanzas"));
    }

    public AlmacenCsv(Path dir) { this.dir = dir; }

    public Path getDirectorio() { return dir; }

    /**
     * Carga los archivos en 'datos'. Las líneas dañadas se ignoran y se deja una copia .bak del archivo.
     *
     * @return cantidad de líneas dañadas ignoradas
     */
    public int cargar(DatosFinanzas datos) throws IOException {
        int malas = 0;
        malas += Math.max(0, leer("movimientos.csv", c -> datos.agregarMovimiento(new Movimiento(
                LocalDate.parse(c[0]), Tipo.valueOf(c[1]), c[2], c[3], Double.parseDouble(c[4])))));
        malas += Math.max(0, leer("metas.csv", c -> datos.agregarMeta(
                new Meta(c[0], Double.parseDouble(c[1]), Double.parseDouble(c[2])))));
        malas += Math.max(0, leer("estimados.csv", c -> datos.setEstimado(YearMonth.parse(c[0]),
                new Estimado(Double.parseDouble(c[1]), Double.parseDouble(c[2])))));
        malas += Math.max(0, leer("tarjetas.csv", c -> datos.agregarTarjeta(new Tarjeta(c[0],
                Double.parseDouble(c[1]), Double.parseDouble(c[2]), Double.parseDouble(c[3]),
                Double.parseDouble(c[4]), Integer.parseInt(c[5])))));

        int r = leer("categorias.csv", c -> datos.agregarCategoria(new Categoria(Tipo.valueOf(c[0]), c[1])));
        if (r < 0) {   // primer arranque con categorías
            datos.cargarCategoriasPorDefecto();
            datos.registrarCategoriasUsadas();
        } else {
            malas += r;
        }
        return malas;
    }

    public void guardar(DatosFinanzas d) throws IOException {
        Files.createDirectories(dir);
        escribir("movimientos.csv", d.getMovimientos().stream().map(m -> String.join(SEP,
                m.fecha().toString(), m.tipo().name(), campo(m.categoria()), campo(m.descripcion()),
                String.valueOf(m.monto()))).collect(Collectors.toList()));
        escribir("metas.csv", d.getMetas().stream().map(m -> String.join(SEP,
                campo(m.getNombre()), String.valueOf(m.getObjetivo()), String.valueOf(m.getAhorrado()))).collect(Collectors.toList()));
        escribir("categorias.csv", d.getCategorias().stream()
                .map(c -> c.tipo().name() + SEP + campo(c.nombre())).collect(Collectors.toList()));
        escribir("estimados.csv", d.getEstimados().entrySet().stream().map(e -> String.join(SEP,
                e.getKey().toString(), String.valueOf(e.getValue().ingresos()),
                String.valueOf(e.getValue().gastos()))).collect(Collectors.toList()));
        escribir("tarjetas.csv", d.getTarjetas().stream().map(t -> String.join(SEP,
                campo(t.getNombre()), String.valueOf(t.getCupo()), String.valueOf(t.getTasaAnual()),
                String.valueOf(t.getSaldo()), String.valueOf(t.getCapitalMinPct()),
                String.valueOf(t.getPlazo()))).collect(Collectors.toList()));
    }

    /** Evita que un texto con ';' rompa el formato del archivo. */
    private static String campo(String texto) {
        return texto.replace(SEP, " ");
    }

    private void escribir(String nombre, List<String> lineas) throws IOException {
        Files.write(dir.resolve(nombre), lineas);
    }

    /** @return -1 si el archivo no existe; si no, la cantidad de líneas dañadas. */
    private int leer(String nombre, Consumer<String[]> alLeer) throws IOException {
        Path p = dir.resolve(nombre);
        if (!Files.exists(p)) return -1;
        int malas = 0;
        for (String linea : Files.readAllLines(p)) {
            if (linea.trim().isEmpty()) continue;
            try {
                alLeer.accept(linea.split(SEP, -1));
            } catch (Exception e) {
                malas++;
            }
        }
        if (malas > 0) Files.copy(p, dir.resolve(nombre + ".bak"), StandardCopyOption.REPLACE_EXISTING);
        return malas;
    }
}
