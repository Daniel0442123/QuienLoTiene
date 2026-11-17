package com.quienlotiene.tiendas.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Consultas de tiendas para responder solicitudes y crear órdenes (Sprint 2: HU-018, HU-019, HU-020 y HU-024).
 * Corre dentro de la transacción de quien la llama.
 */
public interface DatosTiendasApi {

    /**
     * Tienda del usuario con rol TIENDA.
     *
     * @throws com.quienlotiene.shared.domain.ReglaNegocioException TIE-006 si el usuario no tiene tienda
     */
    DatosTienda obtenerTiendaDelDueno(UUID duenoId);

    /** Datos de las tiendas pedidas; las que no existen se omiten. */
    List<DatosTienda> buscarPorIds(Collection<UUID> tiendaIds);

    /**
     * Costo del domicilio hasta el punto, con la tarifa de HU-010 (CU-17 paso 9).
     *
     * @return vacío si la tienda no hace domicilio o el punto está fuera de su cobertura
     */
    Optional<Long> cotizarDomicilio(UUID tiendaId, double latitud, double longitud, long subtotalEnPesos);

    /**
     * Costo del domicilio hasta el punto para crear la orden (HU-024).
     *
     * @throws com.quienlotiene.shared.domain.ReglaNegocioException TIE-010 si no hace domicilio o el punto está fuera
     *     de su cobertura
     */
    long calcularCostoDomicilio(UUID tiendaId, double latitud, double longitud, long subtotalEnPesos);

    /** Si la tienda tiene activo ese medio de pago (HU-012, CP-ORD-024-04). */
    boolean aceptaMedioPago(UUID tiendaId, String medioPago);

    /**
     * Medios de pago activos de cada tienda, en el orden efectivo, transferencia, datáfono (HU-012). Con ellos el
     * cliente solo ve en «Confirmar pedido» los que la tienda acepta (DEF-007, CP-TIE-012-01).
     *
     * @return una entrada por tienda pedida; vacía si la tienda no existe o no configuró sus medios
     */
    Map<UUID, List<String>> mediosPagoDe(Collection<UUID> tiendaIds);
}
