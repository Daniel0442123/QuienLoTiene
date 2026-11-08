package com.quienlotiene.identidad.domain.port;

/** Política de tratamiento de datos que se muestra hoy en el registro. */
public interface PoliticaDatos {

    /** Versión vigente, por ejemplo {@code 1.0}. */
    String versionVigente();
}
