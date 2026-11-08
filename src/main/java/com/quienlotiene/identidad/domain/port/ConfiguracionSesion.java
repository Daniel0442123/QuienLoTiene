package com.quienlotiene.identidad.domain.port;

import java.time.Duration;

/** Parámetros de la sesión que se pueden cambiar sin recompilar. */
public interface ConfiguracionSesion {

    /** Cuánto dura un token de refresco antes de pedir de nuevo la frase de contraseña. */
    Duration duracionTokenRefresco();
}
