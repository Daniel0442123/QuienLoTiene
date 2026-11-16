package com.quienlotiene.solicitudes.domain.model;

/** Lo que contesta la tienda (glosario: respuesta). */
public enum TipoRespuesta {
    /** «Sí lo tengo». */
    SI,
    /** «Tengo algo parecido»: ofrece una alternativa descrita. */
    PARECIDO,
    /** «No lo tengo»: solo cuenta como respuesta recibida. */
    NO;

    public boolean ofreceProducto() {
        return this != NO;
    }
}
