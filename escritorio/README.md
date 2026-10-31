# Mis Finanzas

Aplicación de escritorio (Java 17+, Swing, sin dependencias externas) para gestionar ingresos, gastos,
categorías, estimados mensuales, metas de ahorro y tarjetas de crédito.

Los datos se guardan en archivos CSV dentro de `~/.finanzas/` (en Windows: `C:\Users\TuUsuario\.finanzas\`).

## Estructura del proyecto

```
src/main/java/finanzas/
├── Main.java                      Punto de entrada: carga los datos y abre la ventana
├── modelo/                        Qué datos maneja la aplicación (sin lógica de pantalla ni de archivos)
│   ├── Tipo, Movimiento, Categoria, Estimado, Escenario, CompraTarjeta
│   └── Meta, Tarjeta              Clases con estado y validaciones
├── datos/                         Dónde viven y cómo se guardan los datos
│   ├── DatosFinanzas              Estado en memoria + operaciones (agregar, quitar, consultar)
│   └── AlmacenCsv                 Lee y escribe los archivos CSV
├── servicio/                      Lógica de negocio (cálculos), sin Swing
│   ├── CalculadoraTarjeta         Pago mínimo, cuota fija, simulación de deuda
│   ├── InformeTarjeta             Texto de análisis de una tarjeta
│   ├── ServicioTarjetas           Pago de tarjeta: baja el saldo y lo anota como gasto del mes
│   ├── EstimadorHistorico         Estimado sugerido = promedio de meses anteriores
│   ├── ResumenMensual             Ingresos y gastos por día de un mes
│   └── ResumenFinanciero          Totales, tasa de ahorro, gastos por categoría
├── util/
│   └── Formato                    Conversión y formato de números y textos
└── vista/                         Interfaz gráfica (Swing)
    ├── VentanaPrincipal           Arma las pestañas; guarda y refresca al cambiar los datos
    ├── PanelBase                  Clase base de todas las pestañas
    ├── PanelMovimientos / PanelMes / PanelResumen / PanelMetas / PanelTarjetas / PanelCategorias
    └── Dialogos, Tablas, TextosAyuda
src/test/java/finanzas/
└── PruebasLogica.java             Pruebas de modelo, servicios y almacenamiento (sin librerías)
```

### Cómo fluye la información

1. Un panel (por ejemplo `PanelMovimientos`) modifica `DatosFinanzas`.
2. Ejecuta `alCambiarDatos`, que `VentanaPrincipal` define como: guardar con `AlmacenCsv` y llamar a `refrescar()` en cada panel.
3. Los paneles solo **muestran** datos y reciben acciones del usuario; los cálculos viven en `servicio/`.

Regla de dependencias: `vista` → `servicio` / `datos` → `modelo`. Las capas de abajo no conocen a las de arriba.

## Compilar y ejecutar (PowerShell, desde la carpeta del proyecto)

```powershell
# Compilar
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src\main\java -Filter *.java).FullName

# Ejecutar
java -cp out finanzas.Main

# Pruebas
javac -encoding UTF-8 -cp out -d outtest src\test\java\finanzas\PruebasLogica.java
java -cp "out;outtest" finanzas.PruebasLogica
```

En Linux/macOS: `javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")` y `java -cp out finanzas.Main`
(el separador del classpath de las pruebas es `:`).

## Visual Studio Code

Abre la carpeta del proyecto con *Archivo → Abrir carpeta*. Con la extensión **Extension Pack for Java**
detecta el proyecto por el `pom.xml`. Abre `Main.java` y pulsa **Run** (o F5).

## Crear el ejecutable

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src\main\java -Filter *.java).FullName
mkdir dist
jar cfe dist\MisFinanzas.jar finanzas.Main -C out .
java -jar dist\MisFinanzas.jar
jpackage --type app-image --name MisFinanzas --input dist --main-jar MisFinanzas.jar --main-class finanzas.Main --dest salida
```

Con Maven instalado, `mvn package` genera `target/MisFinanzas.jar`.
