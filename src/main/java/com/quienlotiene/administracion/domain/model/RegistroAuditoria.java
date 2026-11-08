package com.quienlotiene.administracion.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Una acción sensible del administrador (RNF-013, NF-SEG-013): quién, qué, sobre qué recurso y cuándo. No guarda datos
 * personales, solo ids; el detalle es un texto corto como el motivo de un rechazo.
 */
public record RegistroAuditoria(
        UUID id, UUID administradorId, AccionAuditada accion, UUID recursoId, String detalle, Instant fecha) {}
