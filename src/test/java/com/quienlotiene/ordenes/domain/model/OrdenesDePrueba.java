package com.quienlotiene.ordenes.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Órdenes de prueba: 2 unidades a 8.000 con domicilio de 5.000 (CP-ORD-024-01). */
public final class OrdenesDePrueba {

    public static final Instant AHORA = Instant.parse("2026-11-17T15:00:00Z");

    private OrdenesDePrueba() {}

    public static Orden confirmada(UUID clienteId, UUID duenoTiendaId, ModalidadEntrega modalidad) {
        return Orden.crear(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                clienteId,
                new Orden.TiendaDeOrden(UUID.randomUUID(), duenoTiendaId, "Droguería Central", "3001112233"),
                "Paracetamol 500 mg",
                2,
                8_000,
                5_000,
                modalidad,
                MedioPagoOrden.EFECTIVO,
                new ContactoCliente("Ana Pérez", "3109998877"),
                new DireccionDeEntrega("Casa", "Calle 5 # 10-20", "Portón azul", 2.4448, -76.6147),
                AHORA);
    }
}
