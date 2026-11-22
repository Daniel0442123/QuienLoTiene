package com.quienlotiene.solicitudes.domain.model;

import org.jspecify.annotations.Nullable;

/**
 * HU-015 (CU-23): lo que hizo una tienda con las solicitudes de un periodo. Los porcentajes siguen la fórmula aprobada
 * en SC-011 punto 6 y quedan en {@code null} cuando el denominador es 0 (la pantalla muestra «—»).
 *
 * @param recibidas solicitudes que le llegaron
 * @param respondidas respuestas de la tienda de cualquier tipo, incluido «No lo tengo»
 * @param respuestasConProducto respuestas «Sí lo tengo» o «Tengo algo parecido»
 * @param ganadas respuestas que el cliente eligió
 */
public record ActividadDeTienda(int recibidas, int respondidas, int respuestasConProducto, int ganadas) {

    public static final ActividadDeTienda SIN_ACTIVIDAD = new ActividadDeTienda(0, 0, 0, 0);

    /** «Respondiste»: respuestas ÷ solicitudes recibidas, en porcentaje entero. */
    public @Nullable Integer porcentajeRespondiste() {
        return porcentaje(respondidas, recibidas);
    }

    /** «Te eligieron»: respuestas elegidas ÷ respuestas con producto, en porcentaje entero. */
    public @Nullable Integer porcentajeTeEligieron() {
        return porcentaje(ganadas, respuestasConProducto);
    }

    private static @Nullable Integer porcentaje(int parte, int total) {
        return total == 0 ? null : (int) Math.round(100.0 * parte / total);
    }
}
