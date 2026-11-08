package com.quienlotiene.identidad.application.dto;

/**
 * Lo que devuelve el inicio de sesión: la sesión completa (cliente o tienda) o, para el administrador, el paso
 * pendiente del código TOTP (CU-25).
 */
public sealed interface ResultadoInicioSesion permits SesionIniciada, SegundoFactorPendiente {}
