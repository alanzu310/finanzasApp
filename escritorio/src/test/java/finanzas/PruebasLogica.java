package finanzas;

import finanzas.datos.AlmacenCsv;
import finanzas.datos.DatosFinanzas;
import finanzas.modelo.*;
import finanzas.servicio.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Pruebas de la lógica (sin interfaz gráfica ni librerías externas).
 * Ejecutar:  java -cp out finanzas.PruebasLogica   (termina con código 1 si alguna falla)
 * Si luego migras a Maven, estas comprobaciones se pasan fácilmente a JUnit.
 */
public class PruebasLogica {
    private static int total = 0, fallos = 0;

    private static void verificar(String nombre, boolean ok) {
        total++;
        if (!ok) { fallos++; System.out.println("  FALLA: " + nombre); }
    }

    private static boolean cerca(double a, double b) { return Math.abs(a - b) < 0.01; }

    private static boolean lanza(Runnable r) {
        try { r.run(); return false; } catch (IllegalArgumentException e) { return true; }
    }

    private static Movimiento mov(int anio, int mes, int dia, Tipo t, String cat, double monto) {
        return new Movimiento(LocalDate.of(anio, mes, dia), t, cat, "", monto);
    }

    public static void main(String[] args) throws Exception {
        pruebasTarjeta();
        pruebasEstimador();
        pruebasResumenes();
        pruebasDatosYCategorias();
        pruebasPagoTarjeta();
        pruebasModelo();
        pruebasAlmacen();
        System.out.println(total + " comprobaciones, " + fallos + " fallo(s)");
        System.exit(fallos == 0 ? 0 : 1);
    }

    static void pruebasTarjeta() {
        verificar("cuota fija 1000 / 1% / 12 meses = 88.85", cerca(CalculadoraTarjeta.cuotaFija(1000, 0.01, 12), 88.85));
        verificar("cuota fija con tasa 0 = saldo / meses", cerca(CalculadoraTarjeta.cuotaFija(600, 0, 6), 100));
        verificar("cuota fija con saldo 0 = 0", CalculadoraTarjeta.cuotaFija(0, 0.01, 6) == 0);

        double cuota = CalculadoraTarjeta.cuotaFija(1000, 0.01, 12);
        Escenario e = CalculadoraTarjeta.simular("x", 1000, 0.01, s -> cuota);
        verificar("simulación con cuota fija liquida en 12 meses", e.meses() == 12 && !e.truncado());
        verificar("intereses = 12 cuotas - capital", cerca(e.intereses(), 12 * cuota - 1000));

        Tarjeta t = new Tarjeta("Visa", 2000, 16.77, 800, 3, 6);
        verificar("tasa mensual = anual / 12", cerca(t.tasaMensual() * 100, 1.3975));
        verificar("pago mínimo = interés + 3% del saldo", cerca(CalculadoraTarjeta.pagoMinimo(t), 800 * (0.1677 / 12) + 24));
        verificar("uso de cupo 40%", cerca(t.usoCupoPct(), 40));
        verificar("disponible 1200", cerca(t.disponible(), 1200));

        Escenario min = CalculadoraTarjeta.escenarioMinimo(t);
        Escenario rec = CalculadoraTarjeta.escenarioRecomendado(t);
        Escenario tot = CalculadoraTarjeta.escenarioTotal(t);
        verificar("solo mínimo cuesta más intereses que la cuota recomendada", min.intereses() > rec.intereses());
        verificar("pagar el total no genera intereses", tot.intereses() == 0 && tot.totalPagado() == 800);
        verificar("recomendado liquida en el plazo", rec.meses() == 6);

        Tarjeta sinCapital = new Tarjeta("X", 500, 0, 300, 0, 3);
        verificar("con 0% de capital y 0% de tasa la deuda nunca baja (truncado)",
                CalculadoraTarjeta.escenarioMinimo(sinCapital).truncado());

        verificar("informe incluye el nombre y los consejos",
                InformeTarjeta.generar(t).contains("Visa") && InformeTarjeta.generar(t).contains("CONSEJOS"));
        verificar("informe de tarjeta sin saldo",
                InformeTarjeta.generar(new Tarjeta("Vacía", 500, 10, 0, 3, 6)).contains("no tiene saldo"));

        t.registrarCompra(LocalDate.of(2026, 10, 3), "Supermercado", 200);
        verificar("compra suma al saldo", cerca(t.getSaldo(), 1000));
        verificar("la compra queda en el detalle con su descripción",
                t.getCompras().size() == 1 && t.getCompras().get(0).descripcion().equals("Supermercado"));
        verificar("una compra inválida no se anota",
                lanza(() -> t.registrarCompra(LocalDate.now(), "x", 0)) && t.getCompras().size() == 1);
        t.registrarPago(5000);
        verificar("pago mayor no deja saldo negativo", t.getSaldo() == 0);
    }

    static void pruebasEstimador() {
        List<Movimiento> ms = List.of(
                mov(2026, 7, 5, Tipo.INGRESO, "Sueldo", 1000), mov(2026, 7, 9, Tipo.GASTO, "Comida", 400),
                mov(2026, 8, 5, Tipo.INGRESO, "Sueldo", 1200), mov(2026, 8, 9, Tipo.GASTO, "Comida", 600),
                mov(2026, 10, 1, Tipo.GASTO, "Comida", 999));
        var anteriores = EstimadorHistorico.totalesAnteriores(ms, YearMonth.of(2026, 10));
        verificar("solo cuenta meses anteriores con datos (jul y ago)", anteriores.size() == 2);

        var s = EstimadorHistorico.sugerir(ms, YearMonth.of(2026, 10), 6).orElseThrow();
        verificar("promedio de ingresos = 1100", cerca(s.promedioIngresos(), 1100));
        verificar("promedio de gastos = 500", cerca(s.promedioGastos(), 500));

        var ultimo = EstimadorHistorico.sugerir(ms, YearMonth.of(2026, 10), 1).orElseThrow();
        verificar("con 1 mes usa solo el más reciente (agosto)", cerca(ultimo.promedioGastos(), 600));
        verificar("sin historial devuelve vacío", EstimadorHistorico.sugerir(ms, YearMonth.of(2026, 7), 3).isEmpty());
        verificar("la explicación muestra la fórmula", s.explicacion(YearMonth.of(2026, 10), 6).contains("promedio"));
    }

    static void pruebasResumenes() {
        DatosFinanzas d = new DatosFinanzas();
        d.agregarMovimiento(mov(2026, 10, 1, Tipo.INGRESO, "Sueldo", 1000));
        d.agregarMovimiento(mov(2026, 10, 2, Tipo.GASTO, "Comida", 100));
        d.agregarMovimiento(mov(2026, 10, 2, Tipo.GASTO, "Comida", 50));
        d.agregarMovimiento(mov(2026, 10, 3, Tipo.GASTO, "Ocio", 250));
        d.agregarMovimiento(mov(2026, 9, 30, Tipo.GASTO, "Ocio", 999));

        ResumenMensual r = ResumenMensual.de(d.getMovimientos(), YearMonth.of(2026, 10));
        verificar("gastos del día 2 = 150", cerca(r.gastosDia(2), 150));
        verificar("total gastos de octubre = 400 (no incluye septiembre)", cerca(r.totalGastos(), 400));
        verificar("octubre tiene 31 días", r.dias() == 31);

        ResumenFinanciero f = ResumenFinanciero.de(d);
        verificar("balance global", cerca(f.balance(), 1000 - 1399));
        verificar("categoría con más gasto va primero", f.gastosPorCategoria().keySet().iterator().next().equals("Ocio"));
        verificar("tasa de ahorro con ingresos", f.tieneTasaAhorro() && cerca(f.tasaAhorro(), -39.9));
        verificar("sin ingresos no hay tasa de ahorro", !ResumenFinanciero.de(new DatosFinanzas()).tieneTasaAhorro());
    }

    static void pruebasDatosYCategorias() {
        DatosFinanzas d = new DatosFinanzas();
        d.cargarCategoriasPorDefecto();
        verificar("hay categorías de gasto por defecto", d.categoriasDe(Tipo.GASTO).contains("Comida"));
        verificar("no se repite una categoría (sin importar mayúsculas)", !d.agregarCategoria(new Categoria(Tipo.GASTO, "comida")));
        verificar("se puede repetir el nombre en otro tipo", d.agregarCategoria(new Categoria(Tipo.INGRESO, "Comida")));
        verificar("categoría nueva se agrega", d.agregarCategoria(new Categoria(Tipo.GASTO, "Gimnasio")));
        verificar("la lista queda ordenada", d.categoriasDe(Tipo.GASTO).indexOf("Gimnasio") < d.categoriasDe(Tipo.GASTO).indexOf("Ocio"));

        d.agregarMovimiento(mov(2026, 10, 1, Tipo.GASTO, "Gimnasio", 30));
        Categoria g = new Categoria(Tipo.GASTO, "Gimnasio");
        verificar("cuenta los usos de una categoría", d.usosDeCategoria(g) == 1);
        d.eliminarCategoria(g);
        verificar("al quitar la categoría el movimiento se conserva", d.getMovimientos().size() == 1);
        verificar("la categoría ya no está", !d.categoriasDe(Tipo.GASTO).contains("Gimnasio"));

        boolean inmutable;
        try { d.getMovimientos().clear(); inmutable = false; } catch (UnsupportedOperationException e) { inmutable = true; }
        verificar("las listas expuestas no se pueden modificar desde fuera", inmutable);
    }

    static void pruebasPagoTarjeta() {
        DatosFinanzas d = new DatosFinanzas();
        Tarjeta t = new Tarjeta("Visa", 2000, 16.77, 800, 3, 6);
        d.agregarTarjeta(t);
        LocalDate hoy = LocalDate.of(2026, 10, 26);

        Movimiento g = ServicioTarjetas.registrarPago(d, t, 150, hoy);
        verificar("el pago baja el saldo de la tarjeta", cerca(t.getSaldo(), 650));
        verificar("el pago se registra como GASTO del mes", d.getMovimientos().size() == 1
                && g.tipo() == Tipo.GASTO && g.fecha().equals(hoy) && cerca(g.monto(), 150));
        verificar("usa la categoría «Pago de tarjeta»", g.categoria().equals(ServicioTarjetas.CATEGORIA_PAGO));
        verificar("la categoría se crea si no existía", d.categoriasDe(Tipo.GASTO).contains(ServicioTarjetas.CATEGORIA_PAGO));
        verificar("el detalle menciona la tarjeta", g.descripcion().contains("Visa"));
        verificar("cuenta en los gastos del mes", cerca(ResumenMensual.de(d.getMovimientos(), YearMonth.of(2026, 10)).totalGastos(), 150));

        ServicioTarjetas.registrarPago(d, t, 50, hoy);
        verificar("un segundo pago no duplica la categoría",
                d.categoriasDe(Tipo.GASTO).stream().filter(n -> n.equals(ServicioTarjetas.CATEGORIA_PAGO)).count() == 1);

        verificar("un monto inválido se rechaza", lanza(() -> ServicioTarjetas.registrarPago(d, t, 0, hoy)));
        verificar("y no cambia saldo ni movimientos", cerca(t.getSaldo(), 600) && d.getMovimientos().size() == 2);

        ServicioTarjetas.registrarPago(d, t, 5000, hoy);
        verificar("pagar de más deja el saldo en 0 y se registra lo pagado",
                t.getSaldo() == 0 && cerca(d.getMovimientos().get(2).monto(), 5000));
    }

    static void pruebasModelo() {
        verificar("Meta valida objetivo", lanza(() -> new Meta("x", 0, 0)));
        verificar("Meta valida nombre", lanza(() -> new Meta(" ", 10, 0)));
        Meta m = new Meta("Viaje", 1500, 300);
        verificar("progreso 20%", cerca(m.progreso(), 20));
        m.abonar(5000);
        verificar("progreso se limita a 100%", m.progreso() == 100);
        verificar("abono negativo se rechaza", lanza(() -> m.abonar(-1)));
        verificar("Tarjeta valida plazo", lanza(() -> new Tarjeta("x", 1, 1, 0, 3, 0)));
        verificar("Tarjeta valida capital mínimo", lanza(() -> new Tarjeta("x", 1, 1, 0, 101, 6)));
    }

    static void pruebasAlmacen() throws Exception {
        Path dir = Files.createTempDirectory("finanzas-prueba");
        AlmacenCsv almacen = new AlmacenCsv(dir);

        DatosFinanzas a = new DatosFinanzas();
        a.cargarCategoriasPorDefecto();
        a.agregarMovimiento(new Movimiento(LocalDate.of(2026, 10, 5), Tipo.GASTO, "Educación", "Libros; útiles", 45.5));
        a.agregarMeta(new Meta("Viaje", 1500, 300));
        Tarjeta visa = new Tarjeta("Visa", 2000, 16.77, 800, 3, 6);
        visa.registrarCompra(LocalDate.of(2026, 10, 8), "Cena; amigos", 60);
        a.agregarTarjeta(visa);
        a.setEstimado(YearMonth.of(2026, 10), new Estimado(1300, 900));
        almacen.guardar(a);

        DatosFinanzas b = new DatosFinanzas();
        int malas = almacen.cargar(b);
        verificar("guardar y cargar no pierde datos", malas == 0 && b.getMovimientos().size() == 1
                && b.getMetas().size() == 1 && b.getTarjetas().size() == 1 && b.getEstimados().size() == 1);
        verificar("los acentos se conservan", b.getMovimientos().get(0).categoria().equals("Educación"));
        verificar("monto exacto", b.getMovimientos().get(0).monto() == 45.5);
        verificar("estimado exacto", b.getEstimado(YearMonth.of(2026, 10)).orElseThrow().gastos() == 900);
        verificar("categorías guardadas", b.categoriasDe(Tipo.GASTO).contains("Ocio"));
        Tarjeta visaCargada = b.getTarjetas().get(0);
        verificar("las compras de la tarjeta se conservan", visaCargada.getCompras().size() == 1
                && visaCargada.getCompras().get(0).monto() == 60
                && visaCargada.getCompras().get(0).descripcion().equals("Cena  amigos"));
        verificar("cargar las compras no vuelve a sumarlas al saldo", cerca(visaCargada.getSaldo(), 860));

        // Línea dañada: se ignora, se informa y queda copia .bak
        Files.writeString(dir.resolve("metas.csv"), "Viaje;1500.0;300.0\nlinea rota\n");
        DatosFinanzas c = new DatosFinanzas();
        int m2 = almacen.cargar(c);
        verificar("línea dañada se cuenta", m2 == 1);
        verificar("las líneas buenas sí se cargan", c.getMetas().size() == 1);
        verificar("se crea copia .bak", Files.exists(dir.resolve("metas.csv.bak")));

        // Primer arranque con datos viejos (sin categorias.csv): se importan las categorías ya usadas
        Path dir2 = Files.createTempDirectory("finanzas-viejo");
        Files.writeString(dir2.resolve("movimientos.csv"), "2026-10-01;GASTO;Mascotas;Veterinario;45.5\n");
        DatosFinanzas viejo = new DatosFinanzas();
        new AlmacenCsv(dir2).cargar(viejo);
        verificar("migra categorías usadas en datos antiguos", viejo.categoriasDe(Tipo.GASTO).contains("Mascotas"));
        verificar("y agrega las categorías por defecto", viejo.categoriasDe(Tipo.GASTO).contains("Comida"));

        // Carpeta vacía: arranca sin errores
        DatosFinanzas vacio = new DatosFinanzas();
        verificar("carpeta vacía no da error", new AlmacenCsv(Files.createTempDirectory("finanzas-vacio")).cargar(vacio) == 0);
    }
}
