package com.quienlotiene.shared.infrastructure.web;

import java.net.URI;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import org.slf4j.MDC;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

/**
 * Arma los {@link ProblemDetail} (RFC 9457) con la forma acordada en DECISIONES-BACKEND, sección 8: {@code type},
 * {@code title}, {@code status}, {@code detail}, {@code instance}, {@code codigo}, {@code errores} y el
 * {@code idCorrelacion} de la petición. Lo usan el manejador global y los manejadores de Spring Security.
 */
public final class ProblemasHttp {

    public static final String URI_BASE_ERRORES = "https://quienlotiene.app/errores/";

    private ProblemasHttp() {}

    public static ProblemDetail crear(
            HttpStatusCode estado,
            String codigo,
            String titulo,
            String detalle,
            String rutaPeticion,
            List<ErrorDeCampo> errores) {
        var problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setType(URI.create(URI_BASE_ERRORES + aSlug(titulo)));
        problema.setTitle(titulo);
        if (rutaPeticion != null) {
            problema.setInstance(URI.create(rutaPeticion));
        }
        problema.setProperty("codigo", codigo);
        problema.setProperty("errores", List.copyOf(errores));
        var idCorrelacion = MDC.get(FiltroIdCorrelacion.CLAVE_MDC);
        if (idCorrelacion != null) {
            problema.setProperty("idCorrelacion", idCorrelacion);
        }
        return problema;
    }

    public static ProblemDetail crear(ErrorGenerico error, String rutaPeticion) {
        return crear(error.estado(), error.codigo(), error.titulo(), error.detalle(), rutaPeticion, List.of());
    }

    /** "Límite de solicitudes superado" queda como "limite-solicitudes-superado", sin tildes, para usarlo en la URI. */
    static String aSlug(String titulo) {
        var sinTildes = Normalizer.normalize(titulo, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+)|(-+$)", "");
    }
}
