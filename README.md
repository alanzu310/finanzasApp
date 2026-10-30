# Mis Finanzas

Aplicación para gestionar finanzas personales, en Java: **escritorio** (Swing) y **móvil** (Android).

Permite registrar ingresos y gastos con categorías propias, buscar por mes y día, definir estimados mensuales
(con sugerencia a partir del historial), llevar metas de ahorro y analizar tarjetas de crédito
(pago mínimo, recomendado y simulación de intereses).

| Carpeta | Contenido | Cómo abrirlo |
|---|---|---|
| [`escritorio/`](escritorio/) | Versión de escritorio (Java 17+, Swing, sin dependencias) | VS Code o `java -cp out finanzas.Main` |
| [`android/`](android/) | Versión móvil (Android 8.0+) | Android Studio → *Open* |

Cada carpeta tiene su propio `README.md` con instrucciones de compilación y ejecución.

## Estructura de código

Ambas versiones comparten la misma arquitectura por capas y la misma lógica de negocio:

```
modelo/     Datos (Movimiento, Categoria, Meta, Tarjeta, ...)
datos/      Estado en memoria y almacenamiento en CSV
servicio/   Cálculos (tarjetas, estimados, resúmenes), sin interfaz gráfica
util/       Formato y conversión
vista/      Interfaz (Swing en escritorio; fragmentos en Android)
```

> El núcleo (`modelo`, `datos`, `servicio`, `util`) está duplicado en ambas carpetas:
> el de Android está adaptado a Java 8. Las dos copias se prueban con las mismas pruebas.

## Datos

Cada versión guarda sus datos en archivos CSV locales y por ahora **no se sincronizan entre sí**.
Los datos del usuario nunca se incluyen en este repositorio.
