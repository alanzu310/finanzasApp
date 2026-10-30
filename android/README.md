# Mis Finanzas — versión Android

Misma lógica que la versión de escritorio (paquetes `modelo`, `datos`, `servicio`, `util`), con una interfaz nueva
hecha con Android (fragmentos + RecyclerView) en el paquete `finanzas.movil`.

## Abrir en Android Studio
1. *File → Open* y elige esta carpeta (`MisFinanzasAndroid`).
2. Espera a que Gradle sincronice (descarga las dependencias la primera vez; necesita internet).
3. Elige un emulador o conecta tu teléfono (depuración USB) y pulsa **Run ▶**.

Requiere Android 8.0 (API 26) o superior, por el uso de `java.time`.

## Estructura
```
app/src/main/java/finanzas/
├── modelo/ datos/ servicio/ util/   Núcleo (igual que en escritorio, adaptado a Java 8)
└── movil/
    ├── MainActivity                 Pestañas deslizables
    ├── FinanzasViewModel            Mantiene los datos y los guarda en CSV
    └── ui/                          Una pantalla (Fragment) por pestaña + utilidades
app/src/main/res/                    Diseños XML, tema y textos
pruebas/PruebasLogica.java           Pruebas del núcleo (se ejecutan en el PC, sin Android)
```

## Cómo se usa en el teléfono
- Botón **+**: agrega un movimiento, meta, tarjeta o categoría.
- **Mantener pulsado** un elemento: eliminarlo.
- **Tocar** una meta: abonar. **Tocar** una tarjeta: ver análisis y registrar pagos o compras
  (un pago se anota también como gasto del mes).
- En «Mes y estimados», el botón «Estimado del mes» permite escribir o sugerir el estimado.

## Datos
Se guardan en el almacenamiento interno privado de la app (`files/finanzas/*.csv`).
Son independientes de los de la versión de escritorio.

## Crear el APK
*Build → Build Bundle(s) / APK(s) → Build APK(s)*.
