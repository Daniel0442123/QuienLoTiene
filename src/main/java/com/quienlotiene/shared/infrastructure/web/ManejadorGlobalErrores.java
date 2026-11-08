package com.quienlotiene.shared.infrastructure.web;

import com.quienlotiene.shared.domain.ConTiempoDeEspera;
import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Convierte toda excepción que sale de un controlador en un {@link ProblemDetail} con código estable
 * (DECISIONES-BACKEND, sección 8). Nunca envía la traza ni el mensaje técnico al cliente: los errores inesperados
 * quedan en el log con el id de correlación y el cliente recibe un mensaje genérico.
 */
@Slf4j
@RestControllerAdvice
public class ManejadorGlobalErrores extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ReglaNegocioException.class)
    ResponseEntity<ProblemDetail> manejarReglaNegocio(ReglaNegocioException excepcion, HttpServletRequest peticion) {
        var estado = estadoHttpSegunTipo(excepcion.getTipo());
        log.info("Regla de negocio {} incumplida en {}", excepcion.getCodigo(), peticion.getRequestURI());
        var problema = ProblemasHttp.crear(
                estado,
                excepcion.getCodigo(),
                excepcion.getTitulo(),
                excepcion.getMessage(),
                peticion.getRequestURI(),
                List.of());
        var respuesta = ResponseEntity.status(estado);
        if (excepcion instanceof ConTiempoDeEspera conEspera) {
            // Segundos enteros hacia arriba: nunca invitar a reintentar antes de tiempo (RFC 9110, Retry-After)
            var segundos = Math.max(1, (conEspera.tiempoDeEspera().toMillis() + 999) / 1000);
            respuesta.header(HttpHeaders.RETRY_AFTER, String.valueOf(segundos));
        }
        return respuesta.body(problema);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProblemDetail> manejarAccesoDenegado(HttpServletRequest peticion) {
        return respuestaGenerica(ErrorGenerico.SIN_PERMISO, peticion.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ProblemDetail> manejarSinSesion(HttpServletRequest peticion) {
        return respuestaGenerica(ErrorGenerico.SESION_REQUERIDA, peticion.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> manejarErrorInesperado(Exception excepcion, HttpServletRequest peticion) {
        log.error("Error no controlado en {} {}", peticion.getMethod(), peticion.getRequestURI(), excepcion);
        return respuestaGenerica(ErrorGenerico.ERROR_INTERNO, peticion.getRequestURI());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException excepcion,
            HttpHeaders cabeceras,
            HttpStatusCode estado,
            WebRequest peticion) {
        var errores = new ArrayList<ErrorDeCampo>();
        excepcion
                .getBindingResult()
                .getFieldErrors()
                .forEach(error -> errores.add(new ErrorDeCampo(error.getField(), error.getDefaultMessage())));
        excepcion
                .getBindingResult()
                .getGlobalErrors()
                .forEach(error -> errores.add(new ErrorDeCampo(error.getObjectName(), error.getDefaultMessage())));
        return respuestaDatosInvalidos(errores, cabeceras, peticion);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException excepcion,
            HttpHeaders cabeceras,
            HttpStatusCode estado,
            WebRequest peticion) {
        var errores = new ArrayList<ErrorDeCampo>();
        excepcion
                .getParameterValidationResults()
                .forEach(resultado -> resultado
                        .getResolvableErrors()
                        .forEach(error -> errores.add(new ErrorDeCampo(
                                resultado.getMethodParameter().getParameterName(), error.getDefaultMessage()))));
        return respuestaDatosInvalidos(errores, cabeceras, peticion);
    }

    /** Los demás errores que produce Spring MVC (404, 405, 415, cuerpo ilegible...) con su código GEN- en español. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception excepcion, Object cuerpo, HttpHeaders cabeceras, HttpStatusCode estado, WebRequest peticion) {
        var error = ErrorGenerico.segunEstado(estado);
        if (estado.is5xxServerError()) {
            log.error("Error de Spring MVC con estado {}", estado.value(), excepcion);
        }
        var problema = ProblemasHttp.crear(
                estado, error.codigo(), error.titulo(), error.detalle(), rutaDe(peticion), List.of());
        return ResponseEntity.status(estado).headers(cabeceras).body(problema);
    }

    private ResponseEntity<Object> respuestaDatosInvalidos(
            List<ErrorDeCampo> errores, HttpHeaders cabeceras, WebRequest peticion) {
        var error = ErrorGenerico.DATOS_INVALIDOS;
        var problema = ProblemasHttp.crear(
                error.estado(), error.codigo(), error.titulo(), error.detalle(), rutaDe(peticion), errores);
        return ResponseEntity.status(error.estado()).headers(cabeceras).body(problema);
    }

    private static ResponseEntity<ProblemDetail> respuestaGenerica(ErrorGenerico error, String rutaPeticion) {
        return ResponseEntity.status(error.estado()).body(ProblemasHttp.crear(error, rutaPeticion));
    }

    private static String rutaDe(WebRequest peticion) {
        if (peticion instanceof NativeWebRequest peticionNativa
                && peticionNativa.getNativeRequest() instanceof HttpServletRequest peticionHttp) {
            return peticionHttp.getRequestURI();
        }
        return null;
    }

    static HttpStatus estadoHttpSegunTipo(TipoError tipo) {
        return switch (tipo) {
            case DATOS_INVALIDOS -> HttpStatus.BAD_REQUEST;
            case NO_AUTENTICADO -> HttpStatus.UNAUTHORIZED;
            case SIN_PERMISO -> HttpStatus.FORBIDDEN;
            case NO_ENCONTRADO -> HttpStatus.NOT_FOUND;
            case CONFLICTO -> HttpStatus.CONFLICT;
            case REGLA_NEGOCIO -> HttpStatus.UNPROCESSABLE_CONTENT;
            case DEMASIADOS_INTENTOS -> HttpStatus.TOO_MANY_REQUESTS;
        };
    }
}
