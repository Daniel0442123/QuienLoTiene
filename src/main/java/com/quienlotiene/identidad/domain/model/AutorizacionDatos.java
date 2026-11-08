package com.quienlotiene.identidad.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Autorización de tratamiento de datos personales (Ley 1581, HU-005, NF-SEG-014): qué versión de la política aceptó
 * el usuario y cuándo. Sin ella no se crea la cuenta.
 */
public final class AutorizacionDatos {

    private final UUID id;
    private final UUID usuarioId;
    private final String versionPolitica;
    private final Instant fechaAceptacion;

    private AutorizacionDatos(UUID id, UUID usuarioId, String versionPolitica, Instant fechaAceptacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.versionPolitica = versionPolitica;
        this.fechaAceptacion = fechaAceptacion;
    }

    /**
     * Registra la aceptación de la política vigente.
     *
     * @throws AutorizacionDatosRequeridaException si el usuario no marcó la casilla (CP-IDE-005-01)
     * @throws VersionPoliticaDesactualizadaException si aceptó una versión distinta a la vigente
     */
    public static AutorizacionDatos otorgar(
            UUID id,
            UUID usuarioId,
            boolean aceptoPolitica,
            String versionAceptada,
            String versionVigente,
            Instant ahora) {
        if (!aceptoPolitica) {
            throw new AutorizacionDatosRequeridaException();
        }
        if (versionAceptada == null || !versionAceptada.strip().equals(versionVigente)) {
            throw new VersionPoliticaDesactualizadaException(versionVigente);
        }
        return new AutorizacionDatos(id, usuarioId, versionVigente, ahora);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getVersionPolitica() {
        return versionPolitica;
    }

    public Instant getFechaAceptacion() {
        return fechaAceptacion;
    }
}
