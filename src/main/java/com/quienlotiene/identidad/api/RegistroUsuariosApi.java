package com.quienlotiene.identidad.api;

import java.util.UUID;

/**
 * Fachada para que otros módulos creen cuentas sin conocer el dominio de identidad. La usa {@code tiendas} al
 * registrar una tienda (HU-002, CU-15 pasos 5 a 7). Corre dentro de la transacción de quien la llama: si el registro
 * de la tienda falla, la cuenta tampoco queda creada.
 */
public interface RegistroUsuariosApi {

    /**
     * Crea la cuenta del dueño con rol {@code TIENDA} y su autorización de datos (HU-005).
     *
     * @return id del usuario creado
     * @throws com.quienlotiene.shared.domain.ReglaNegocioException con códigos IDE- si el correo ya existe, la frase es
     *     débil o falta la autorización de datos
     */
    UUID registrarDuenoTienda(NuevoDuenoTienda dueno);
}
