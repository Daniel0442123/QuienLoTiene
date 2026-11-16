package com.quienlotiene.mensajeria.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @param nombre de la tienda (para el cliente) o del cliente (para la tienda)
 * @param telefono formato internacional sin «+», por ejemplo 573001234567
 */
@Schema(name = "ContactoWhatsappResponse")
public record ContactoWhatsapp(String nombre, String telefono, String enlace) {

    @Override
    public String toString() {
        return "ContactoWhatsapp[***]";
    }
}
