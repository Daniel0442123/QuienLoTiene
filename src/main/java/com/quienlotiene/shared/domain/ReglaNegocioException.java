package com.quienlotiene.shared.domain;

import java.util.Objects;

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

    protected ReglaNegocioException(String codigo, String titulo, String detalle, TipoError tipo) {
        super(exigirTexto(detalle, "detalle"));
        this.codigo = exigirTexto(codigo, "codigo");
        this.titulo = exigirTexto(titulo, "titulo");
        this.tipo = Objects.requireNonNull(tipo, "El campo tipo de la excepción es obligatorio");
    }

    private static String exigirTexto(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " de la excepción es obligatorio");
        }
        return valor;
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
