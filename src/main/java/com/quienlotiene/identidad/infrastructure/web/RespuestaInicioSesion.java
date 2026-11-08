package com.quienlotiene.identidad.infrastructure.web;

/**
 * Cuerpo de {@code POST /api/v1/auth/login}: la sesión, o el aviso de que falta el código TOTP del administrador. El
 * frontend lo distingue con {@code segundoFactorRequerido}.
 */
public sealed interface RespuestaInicioSesion permits SesionResponse, SegundoFactorPendienteResponse {

    boolean segundoFactorRequerido();
}
