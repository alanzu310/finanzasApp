package finanzas.movil.ui;

/** Una fila de una lista: texto principal, texto secundario, valor a la derecha y el objeto que representa. */
public final class Fila {
    public final String titulo;
    public final String subtitulo;
    public final String derecha;
    /** Color del texto de la derecha; 0 = color por defecto. */
    public final int colorDerecha;
    public final Object dato;

    public Fila(String titulo, String subtitulo, String derecha, int colorDerecha, Object dato) {
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.derecha = derecha;
        this.colorDerecha = colorDerecha;
        this.dato = dato;
    }

    public Fila(String titulo, String subtitulo, String derecha, Object dato) {
        this(titulo, subtitulo, derecha, 0, dato);
    }
}
