package com.quienlotiene.mensajeria.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Chat de una orden (HU-033).
 *
 * @param soloLectura {@code true} cuando la orden ya terminó: se puede leer pero no escribir (MEN-002)
 */
public record ChatDeOrden(UUID ordenId, boolean soloLectura, List<MensajeVista> mensajes) {}
