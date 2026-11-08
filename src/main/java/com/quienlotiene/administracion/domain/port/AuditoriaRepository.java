package com.quienlotiene.administracion.domain.port;

import com.quienlotiene.administracion.domain.model.RegistroAuditoria;

/** Guarda los registros de auditoría en {@code administracion.auditoria}; nunca se editan ni se borran. */
public interface AuditoriaRepository {

    void registrar(RegistroAuditoria registro);
}
