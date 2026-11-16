package com.quienlotiene.solicitudes.domain.model;

import com.quienlotiene.shared.domain.Ubicacion;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/** Datos de prueba compartidos por las pruebas del Sprint 2 de solicitudes. */
public final class SolicitudesDePrueba {

    public static final Instant AHORA = Instant.parse("2026-11-17T15:00:00Z");
    public static final Ubicacion POPAYAN = new Ubicacion(2.4448, -76.6147);

    private SolicitudesDePrueba() {}

    /** Solicitud ABIERTA de 2 unidades de paracetamol publicada hace 5 minutos; vence en 25. */
    public static Solicitud abierta(UUID clienteId, Modalidad modalidad) {
        return Solicitud.publicar(
                UUID.randomUUID(),
                clienteId,
                UUID.randomUUID(),
                "Paracetamol 500 mg",
                2,
                modalidad,
                false,
                POPAYAN,
                3_000,
                Duration.ofMinutes(30),
                AHORA.minus(Duration.ofMinutes(5)));
    }

    public static Solicitud abierta() {
        return abierta(UUID.randomUUID(), Modalidad.DOMICILIO);
    }

    public static Respuesta si(Solicitud solicitud, UUID tiendaId, long precio, int unidades) {
        return Respuesta.responder(
                UUID.randomUUID(),
                solicitud,
                tiendaId,
                UUID.randomUUID(),
                TipoRespuesta.SI,
                precio,
                unidades,
                null,
                20,
                null,
                AHORA);
    }

    public static Respuesta no(Solicitud solicitud, UUID tiendaId) {
        return Respuesta.responder(
                UUID.randomUUID(),
                solicitud,
                tiendaId,
                UUID.randomUUID(),
                TipoRespuesta.NO,
                null,
                null,
                null,
                null,
                null,
                AHORA);
    }
}
