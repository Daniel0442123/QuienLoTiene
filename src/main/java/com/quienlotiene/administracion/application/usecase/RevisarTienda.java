package com.quienlotiene.administracion.application.usecase;

import com.quienlotiene.administracion.domain.model.AccionAuditada;
import com.quienlotiene.administracion.domain.model.RegistroAuditoria;
import com.quienlotiene.administracion.domain.port.AuditoriaRepository;
import com.quienlotiene.tiendas.api.DocumentoDescargable;
import com.quienlotiene.tiendas.api.RevisionTiendasApi;
import com.quienlotiene.tiendas.api.TiendaParaRevision;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-036 Aprobar o rechazar tiendas (CU-26). La regla de estados la aplica tiendas; aquí se registra la auditoría de
 * cada decisión y de cada documento abierto (RNF-013) en la misma transacción.
 */
@Service
public class RevisarTienda {

    private final RevisionTiendasApi revisionTiendas;
    private final AuditoriaRepository auditoriaRepository;
    private final Clock reloj;

    RevisarTienda(RevisionTiendasApi revisionTiendas, AuditoriaRepository auditoriaRepository, Clock reloj) {
        this.revisionTiendas = revisionTiendas;
        this.auditoriaRepository = auditoriaRepository;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<TiendaParaRevision> listar(String estado) {
        return revisionTiendas.listarPorEstado(estado);
    }

    @Transactional
    public DocumentoDescargable descargarDocumento(UUID administradorId, UUID tiendaId, UUID documentoId) {
        var documento = revisionTiendas.obtenerDocumento(tiendaId, documentoId);
        auditar(administradorId, AccionAuditada.DOCUMENTO_TIENDA_DESCARGADO, tiendaId, documentoId.toString());
        return documento;
    }

    @Transactional
    public TiendaParaRevision aprobar(UUID administradorId, UUID tiendaId) {
        var tienda = revisionTiendas.aprobar(tiendaId);
        auditar(administradorId, AccionAuditada.TIENDA_APROBADA, tiendaId, null);
        return tienda;
    }

    @Transactional
    public TiendaParaRevision rechazar(UUID administradorId, UUID tiendaId, String motivo) {
        var tienda = revisionTiendas.rechazar(tiendaId, motivo);
        auditar(administradorId, AccionAuditada.TIENDA_RECHAZADA, tiendaId, tienda.motivoRechazo());
        return tienda;
    }

    private void auditar(UUID administradorId, AccionAuditada accion, UUID recursoId, String detalle) {
        auditoriaRepository.registrar(
                new RegistroAuditoria(UUID.randomUUID(), administradorId, accion, recursoId, detalle, reloj.instant()));
    }
}
