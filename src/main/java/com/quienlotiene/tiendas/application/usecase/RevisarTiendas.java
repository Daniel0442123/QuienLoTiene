package com.quienlotiene.tiendas.application.usecase;

import com.quienlotiene.shared.domain.AlmacenArchivos;
import com.quienlotiene.tiendas.api.DocumentoDescargable;
import com.quienlotiene.tiendas.api.RevisionTiendasApi;
import com.quienlotiene.tiendas.api.TiendaParaRevision;
import com.quienlotiene.tiendas.api.TiendaRevisadaEvent;
import com.quienlotiene.tiendas.domain.model.EstadoTienda;
import com.quienlotiene.tiendas.domain.model.Tienda;
import com.quienlotiene.tiendas.domain.model.TiendaNoEncontradaException;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-036 del lado de tiendas (CU-26): lista la bandeja, entrega los documentos y aplica la aprobación o el rechazo con
 * las reglas del dominio. Lo usa administracion por {@link RevisionTiendasApi}.
 */
@Service
class RevisarTiendas implements RevisionTiendasApi {

    private final TiendaRepository tiendaRepository;
    private final AlmacenArchivos almacenArchivos;
    private final ApplicationEventPublisher publicadorEventos;
    private final Clock reloj;

    RevisarTiendas(
            TiendaRepository tiendaRepository,
            AlmacenArchivos almacenArchivos,
            ApplicationEventPublisher publicadorEventos,
            Clock reloj) {
        this.tiendaRepository = tiendaRepository;
        this.almacenArchivos = almacenArchivos;
        this.publicadorEventos = publicadorEventos;
        this.reloj = reloj;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TiendaParaRevision> listarPorEstado(String estado) {
        var estadoPedido = EstadoTienda.valueOf(estado.toUpperCase(Locale.ROOT));
        return tiendaRepository.listarPorEstado(estadoPedido).stream()
                .map(this::aTiendaParaRevision)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoDescargable obtenerDocumento(UUID tiendaId, UUID documentoId) {
        var documento = tiendaRepository.listarDocumentos(tiendaId).stream()
                .filter(documentoDeLaTienda -> documentoDeLaTienda.id().equals(documentoId))
                .findFirst()
                .orElseThrow(TiendaNoEncontradaException::new);
        var extension = documento.ruta().substring(documento.ruta().lastIndexOf('.'));
        return new DocumentoDescargable(
                documento.tipo().name().toLowerCase(Locale.ROOT) + extension,
                documento.tipoContenido(),
                almacenArchivos.leer(documento.ruta()));
    }

    @Override
    @Transactional
    public TiendaParaRevision aprobar(UUID tiendaId) {
        return revisar(tiendaId, tienda -> tienda.aprobar(reloj.instant()));
    }

    @Override
    @Transactional
    public TiendaParaRevision rechazar(UUID tiendaId, String motivo) {
        return revisar(tiendaId, tienda -> tienda.rechazar(motivo, reloj.instant()));
    }

    private TiendaParaRevision revisar(UUID tiendaId, Consumer<Tienda> decision) {
        var tienda = tiendaRepository.buscarPorId(tiendaId).orElseThrow(TiendaNoEncontradaException::new);
        decision.accept(tienda);
        tiendaRepository.guardarRevision(tienda);
        publicadorEventos.publishEvent(new TiendaRevisadaEvent(
                tienda.getId(),
                tienda.getDuenoId(),
                tienda.getEstado().name(),
                tienda.getMotivoRechazo().orElse(null),
                tienda.getFechaRevision().orElseThrow()));
        return aTiendaParaRevision(tienda);
    }

    private TiendaParaRevision aTiendaParaRevision(Tienda tienda) {
        var documentos = tiendaRepository.listarDocumentos(tienda.getId()).stream()
                .map(documento -> new TiendaParaRevision.DocumentoResumen(
                        documento.id(), documento.tipo().name(), documento.tipoContenido(), documento.tamanoEnBytes()))
                .toList();
        return new TiendaParaRevision(
                tienda.getId(),
                tienda.getNombre(),
                tienda.getCategoriaId(),
                tienda.getTelefono(),
                tienda.getUbicacion().latitud(),
                tienda.getUbicacion().longitud(),
                tienda.getEstado().name(),
                tienda.getFechaRegistro(),
                tienda.getMotivoRechazo().orElse(null),
                tienda.getFechaRevision().orElse(null),
                documentos);
    }
}
