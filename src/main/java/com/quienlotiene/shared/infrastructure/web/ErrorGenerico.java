package com.quienlotiene.shared.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/** Errores que no vienen de una regla de negocio sino del protocolo HTTP o de un fallo inesperado (prefijo GEN-). */
public enum ErrorGenerico {
    DATOS_INVALIDOS(
            "GEN-001",
            HttpStatus.BAD_REQUEST,
            "Datos inválidos",
            "Algunos datos no son válidos. Revisa la lista de errores."),
    PETICION_MAL_FORMADA(
            "GEN-002",
            HttpStatus.BAD_REQUEST,
            "Petición mal formada",
            "La petición no se pudo interpretar. Revisa el formato de los datos enviados."),
    SESION_REQUERIDA("GEN-003", HttpStatus.UNAUTHORIZED, "Sesión requerida", "Debes iniciar sesión para continuar."),
    SIN_PERMISO("GEN-004", HttpStatus.FORBIDDEN, "Sin permiso", "No tienes permiso para realizar esta acción."),
    RECURSO_NO_ENCONTRADO("GEN-005", HttpStatus.NOT_FOUND, "Recurso no encontrado", "El recurso solicitado no existe."),
    METODO_NO_PERMITIDO(
            "GEN-006",
            HttpStatus.METHOD_NOT_ALLOWED,
            "Método no permitido",
            "Esta operación no está disponible para el recurso solicitado."),
    FORMATO_NO_SOPORTADO(
            "GEN-007",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Formato no soportado",
            "El formato de los datos enviados o pedidos no está soportado."),
    ERROR_INTERNO(
            "GEN-008",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Error interno",
            "Ocurrió un error inesperado. Intenta de nuevo más tarde.");

    private final String codigo;
    private final HttpStatus estado;
    private final String titulo;
    private final String detalle;

    ErrorGenerico(String codigo, HttpStatus estado, String titulo, String detalle) {
        this.codigo = codigo;
        this.estado = estado;
        this.titulo = titulo;
        this.detalle = detalle;
    }

    /** El error genérico que corresponde a un estado HTTP producido por Spring (404, 405, 415, ...). */
    public static ErrorGenerico segunEstado(HttpStatusCode estadoHttp) {
        return switch (estadoHttp.value()) {
            case 401 -> SESION_REQUERIDA;
            case 403 -> SIN_PERMISO;
            case 404 -> RECURSO_NO_ENCONTRADO;
            case 405 -> METODO_NO_PERMITIDO;
            case 406, 415 -> FORMATO_NO_SOPORTADO;
            default -> estadoHttp.is5xxServerError() ? ERROR_INTERNO : PETICION_MAL_FORMADA;
        };
    }

    public String codigo() {
        return codigo;
    }

    public HttpStatus estado() {
        return estado;
    }

    public String titulo() {
        return titulo;
    }

    public String detalle() {
        return detalle;
    }
}
