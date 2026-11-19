package com.quienlotiene.reputacion.application.usecase;

import com.quienlotiene.ordenes.api.OrdenesApi;
import com.quienlotiene.reputacion.application.dto.CalificarComando;
import com.quienlotiene.reputacion.application.dto.ResenaVista;
import com.quienlotiene.reputacion.domain.model.CalificacionNoPermitidaException;
import com.quienlotiene.reputacion.domain.model.Resena;
import com.quienlotiene.reputacion.domain.model.ResenaDuplicadaException;
import com.quienlotiene.reputacion.domain.model.ResenaNoEncontradaException;
import com.quienlotiene.reputacion.domain.port.ResenaRepository;
import java.time.Clock;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-034 Calificar tienda (CU-10, diagrama 16): solo el cliente de una orden RESUELTA y una vez por orden. El promedio
 * de la tienda se recalcula al consultarla (contrato del Sprint 3, decisión 3).
 */
@Slf4j
@Service
public class CalificarTienda {

    private static final String RESUELTA = "RESUELTA";

    private final OrdenesApi ordenesApi;
    private final ResenaRepository resenaRepository;
    private final Clock reloj;

    CalificarTienda(OrdenesApi ordenesApi, ResenaRepository resenaRepository, Clock reloj) {
        this.ordenesApi = ordenesApi;
        this.resenaRepository = resenaRepository;
        this.reloj = reloj;
    }

    /**
     * @throws CalificacionNoPermitidaException REP-001 si la orden no está RESUELTA o quien califica es la tienda
     *     (CP-REP-034-02)
     * @throws ResenaDuplicadaException REP-002 (CP-REP-034-03)
     */
    @Transactional
    public ResenaVista ejecutar(CalificarComando comando) {
        var orden = ordenesApi.obtenerResumen(comando.ordenId(), comando.clienteId());
        if (!orden.esDelCliente(comando.clienteId()) || !RESUELTA.equals(orden.estado())) {
            throw new CalificacionNoPermitidaException();
        }
        if (resenaRepository.existeParaOrden(orden.ordenId())) {
            throw new ResenaDuplicadaException();
        }
        var resena = new Resena(
                UUID.randomUUID(),
                orden.ordenId(),
                orden.tiendaId(),
                comando.clienteId(),
                comando.estrellas(),
                comando.comentario(),
                reloj.instant());
        resenaRepository.guardar(resena);
        log.info("Reseña de {} estrellas para la tienda {}", resena.estrellas(), resena.tiendaId());
        return aVista(resena);
    }

    /**
     * La reseña de una orden, para el cliente o la tienda de esa orden.
     *
     * @throws ResenaNoEncontradaException REP-003 si todavía no tiene
     */
    @Transactional(readOnly = true)
    public ResenaVista consultar(UUID usuarioId, UUID ordenId) {
        var orden = ordenesApi.obtenerResumen(ordenId, usuarioId);
        return resenaRepository
                .buscarPorOrden(orden.ordenId())
                .map(CalificarTienda::aVista)
                .orElseThrow(() -> new ResenaNoEncontradaException("La reseña de esta orden"));
    }

    private static ResenaVista aVista(Resena resena) {
        return new ResenaVista(
                resena.id(),
                resena.ordenId(),
                resena.tiendaId(),
                resena.estrellas(),
                resena.comentario(),
                resena.fecha());
    }
}
