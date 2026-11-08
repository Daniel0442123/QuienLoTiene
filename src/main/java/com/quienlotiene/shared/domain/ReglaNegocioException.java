package com.quienlotiene.shared.domain;

/**
 * Excepción base de todas las reglas de negocio. Cada módulo la extiende con su propia excepción y un código estable
 * registrado en {@code docs/api/CODIGOS-DE-ERROR.md} (por ejemplo {@code SOL-001}).
 *
 * <p>El mensaje ({@link #getMessage()}) se muestra al usuario tal cual: va en español y nunca lleva datos personales.
 */
public abstract class ReglaNegocioException extends RuntimeException {

    private final String codigo;
    private final String titulo;
    private final TipoError tipo;

    /**
     * Las subclases pasan siempre textos fijos. No se validan aquí: lanzar una excepción desde este constructor
     * dejaría el objeto a medio construir (SpotBugs CT_CONSTRUCTOR_THROW).
     */
    protected ReglaNegocioException(String codigo, String titulo, String detalle, TipoError tipo) {
        super(detalle);
        this.codigo = codigo;
        this.titulo = titulo;
        this.tipo = tipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public TipoError getTipo() {
        return tipo;
    }
}
