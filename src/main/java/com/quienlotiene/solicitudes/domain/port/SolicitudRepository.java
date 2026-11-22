package com.quienlotiene.solicitudes.domain.port;

import com.quienlotiene.solicitudes.domain.model.Solicitud;
import com.quienlotiene.solicitudes.domain.model.SolicitudParaTienda;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Guarda y consulta las solicitudes del esquema {@code solicitudes}. */
public interface SolicitudRepository {

    void guardarNueva(Solicitud solicitud);

    /** Fechas de publicación de las solicitudes del cliente desde {@code desde} (para el límite por hora). */
    List<Instant> listarFechasPublicacionDesde(UUID clienteId, Instant desde);

    Optional<Solicitud> buscarPorId(UUID solicitudId);

    /** Guarda el estado (RESUELTA o VENCIDA) con bloqueo optimista. */
    void guardarEstado(Solicitud solicitud);

    /**
     * HU-018: ABIERTAS y sin vencer en {@code ahora} que llegaron a la tienda y que la tienda no ha respondido.
     * Primero las urgentes y luego de la más reciente a la más antigua.
     */
    List<SolicitudParaTienda> listarDisponiblesParaTienda(UUID tiendaId, Instant ahora);

    /**
     * CU-29: toma hasta {@code limite} solicitudes ABIERTAS ya vencidas en {@code ahora}, bloqueadas con
     * {@code FOR UPDATE SKIP LOCKED} para que dos ejecuciones nunca procesen la misma (D-19).
     */
    List<Solicitud> tomarVencidas(Instant ahora, int limite);

    /**
     * CU-31: toma hasta {@code limite} solicitudes ABIERTAS sin vencer en {@code ahora}, publicadas hasta
     * {@code publicadasHasta}, que no se han ampliado y no tienen ninguna respuesta, con {@code FOR UPDATE SKIP LOCKED}.
     */
    List<Solicitud> tomarParaAmpliar(Instant publicadasHasta, Instant ahora, int limite);

    /** HU-022: guarda el radio ampliado y la marca de que ya se amplió, con bloqueo optimista. */
    void guardarRadio(Solicitud solicitud);

    /** HU-008: solicitudes del cliente de la más reciente a la más antigua, por páginas. */
    List<Solicitud> listarDelCliente(UUID clienteId, int pagina, int tamano);
}
