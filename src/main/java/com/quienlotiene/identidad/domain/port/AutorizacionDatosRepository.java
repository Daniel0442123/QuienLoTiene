package com.quienlotiene.identidad.domain.port;

import com.quienlotiene.identidad.domain.model.AutorizacionDatos;

/** Guarda las autorizaciones de tratamiento de datos (prueba de cumplimiento de la Ley 1581). */
public interface AutorizacionDatosRepository {

    void guardar(AutorizacionDatos autorizacion);
}
