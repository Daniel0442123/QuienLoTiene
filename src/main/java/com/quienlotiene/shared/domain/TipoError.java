package com.quienlotiene.shared.domain;

/**
 * Clase de falla de una regla de negocio. El dominio no conoce HTTP: el manejador global de errores traduce cada
 * tipo al estado HTTP que corresponde (DECISIONES-BACKEND, sección 7).
 */
public enum TipoError {
    /** Los datos tienen la forma correcta pero no cumplen una regla (400). */
    DATOS_INVALIDOS,
    /** Falta iniciar sesión o la sesión no es válida (401). */
    NO_AUTENTICADO,
    /** El usuario no tiene el rol necesario (403). */
    SIN_PERMISO,
    /** No existe, o existe pero es de otro usuario (404, riesgo R-03). */
    NO_ENCONTRADO,
    /** El estado actual no permite la acción, por ejemplo un correo ya registrado (409). */
    CONFLICTO,
    /** Una regla del negocio impide la acción, por ejemplo el límite de solicitudes por hora (422). */
    REGLA_NEGOCIO,
    /** Demasiados intentos en poco tiempo (429). */
    DEMASIADOS_INTENTOS
}
