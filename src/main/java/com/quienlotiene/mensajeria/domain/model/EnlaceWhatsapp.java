package com.quienlotiene.mensajeria.domain.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

/**
 * Enlace {@code wa.me} con el teléfono y un mensaje que trae el producto y el número corto de la orden (CU-12,
 * diagrama 18 paso 2). Los celulares de Colombia (10 dígitos que empiezan por 3) se completan con el indicativo 57.
 */
public record EnlaceWhatsapp(String telefono, String enlace) {

    public static final String INDICATIVO_COLOMBIA = "57";
    private static final int DIGITOS_CELULAR_COLOMBIA = 10;
    private static final int LONGITUD_NUMERO_CORTO = 8;

    public static EnlaceWhatsapp para(String telefonoEscrito, UUID ordenId, String producto) {
        var telefono = normalizar(telefonoEscrito);
        var mensaje =
                "Hola, te escribo por la orden " + numeroCorto(ordenId) + " de " + producto + " en ¿Quién lo tiene?";
        return new EnlaceWhatsapp(
                telefono,
                "https://wa.me/" + telefono + "?text="
                        + URLEncoder.encode(mensaje, StandardCharsets.UTF_8).replace("+", "%20"));
    }

    /** Solo dígitos, con indicativo: {@code 300 123 4567} → {@code 573001234567}. */
    static String normalizar(String telefonoEscrito) {
        var digitos = telefonoEscrito == null ? "" : telefonoEscrito.replaceAll("\\D", "");
        if (digitos.length() == DIGITOS_CELULAR_COLOMBIA && digitos.startsWith("3")) {
            return INDICATIVO_COLOMBIA + digitos;
        }
        return digitos;
    }

    /** Los primeros 8 caracteres del id en mayúsculas, como se muestran en la app. */
    static String numeroCorto(UUID ordenId) {
        return ordenId.toString().substring(0, LONGITUD_NUMERO_CORTO).toUpperCase(Locale.ROOT);
    }
}
