package com.quienlotiene.tiendas.api;

import java.util.UUID;

/**
 * Resultado de suspender o reactivar una tienda (HU-037). Trae el dueño para que administracion suspenda también su
 * cuenta.
 */
public record TiendaSuspendida(TiendaParaRevision tienda, UUID duenoId) {}
