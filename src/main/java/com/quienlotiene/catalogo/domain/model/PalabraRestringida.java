package com.quienlotiene.catalogo.domain.model;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Palabra de la lista de productos restringidos (glosario: producto restringido). Se guarda normalizada: minúsculas, sin
 * tildes y con un solo espacio, para comparar «Amoxicilina 500 mg» con «amoxicilina» (contrato del Sprint 3, decisión
 * 5).
 */
public record PalabraRestringida(UUID id, String palabraClave) {

    public static final int MINIMO = 2;
    public static final int MAXIMO = 80;

    public PalabraRestringida {
        Objects.requireNonNull(id, "id");
        palabraClave = normalizar(palabraClave);
        if (palabraClave.length() < MINIMO || palabraClave.length() > MAXIMO) {
            throw new PalabraRestringidaInvalidaException();
        }
    }

    /** HU-023: el producto contiene la palabra (los dos normalizados). */
    public boolean apareceEn(String producto) {
        return normalizar(producto).contains(palabraClave);
    }

    /** Minúsculas, sin tildes ni diéresis, sin espacios repetidos. {@code null} queda vacío. */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        var sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).strip().replaceAll("\\s+", " ");
    }
}
