package com.quienlotiene.shared.infrastructure.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;

/**
 * DEF-006 (C-04): deja {@code /v3/api-docs} listo para generar los tipos del frontend con {@code npm run api:tipos}.
 *
 * <ul>
 *   <li>A2 y RNF-024: las respuestas 2xx salen como {@code application/json} y no {@code *}{@code /*}.
 *   <li>Los errores 4xx y 5xx salen como {@code application/problem+json} con el esquema {@code ProblemDetail} de
 *       DECISIONES-BACKEND §8. Antes springdoc les ponía el esquema de la respuesta exitosa.
 *   <li>A6: los enums del dominio se publican con nombre ({@code EstadoOrden}, {@code MedioPago}…), como en
 *       {@code docs/api/contrato-sprint-2.yaml}. En Java son {@code String} con {@code allowableValues}, así que se
 *       reconocen por sus valores: un campo con exactamente esos valores pasa a ser una referencia al enum.
 * </ul>
 */
public class ContratoOpenApiCustomizer implements OpenApiCustomizer {

    static final String JSON = "application/json";
    static final String PROBLEMA_JSON = "application/problem+json";
    private static final String TODOS = "*/*";
    private static final String PREFIJO_REFERENCIA = "#/components/schemas/";

    /** Enums con nombre del contrato. El orden de los valores es el que publica el backend. */
    static final Map<String, List<String>> ENUMS = enums();

    private static Map<String, List<String>> enums() {
        var enums = new LinkedHashMap<String, List<String>>();
        enums.put("Modalidad", List.of("RECOGER", "DOMICILIO"));
        enums.put("MedioPago", List.of("EFECTIVO", "TRANSFERENCIA", "DATAFONO"));
        enums.put("TipoRespuesta", List.of("SI", "PARECIDO", "NO"));
        enums.put("EstadoSolicitud", List.of("ABIERTA", "RESUELTA", "VENCIDA"));
        enums.put("EstadoRespuesta", List.of("ESPERANDO", "ELEGIDA", "NO_ELEGIDA", "VENCIDA"));
        enums.put(
                "EstadoOrden",
                List.of(
                        "CONFIRMADO",
                        "PREPARANDO",
                        "EN_CAMINO",
                        "LISTO_PARA_RECOGER",
                        "ENTREGADO",
                        "RESUELTA",
                        "CANCELADO",
                        "VENCIDA"));
        enums.put("EstadoTienda", List.of("PENDIENTE", "APROBADA", "RECHAZADA", "SUSPENDIDA"));
        enums.put("DiaSemana", List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"));
        return Map.copyOf(enums);
    }

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }
        var version = openApi.getSpecVersion();
        var componentes = openApi.getComponents();
        if (componentes.getSchemas() != null) {
            componentes.getSchemas().values().forEach(esquema -> nombrarEnums(esquema, version, new HashSet<>()));
        }
        if (openApi.getPaths() != null) {
            openApi.getPaths()
                    .values()
                    .forEach(ruta -> ruta.readOperations().forEach(operacion -> ajustarOperacion(operacion, version)));
        }
        ENUMS.forEach((nombre, valores) -> componentes.addSchemas(nombre, esquemaEnum(valores, version)));
        componentes.addSchemas("ErrorDeCampo", esquemaErrorDeCampo(version));
        componentes.addSchemas("ProblemDetail", esquemaProblema(version));
    }

    private static void ajustarOperacion(Operation operacion, SpecVersion version) {
        if (operacion.getParameters() != null) {
            for (Parameter parametro : operacion.getParameters()) {
                nombrarEnums(parametro.getSchema(), version, new HashSet<>());
            }
        }
        if (operacion.getRequestBody() != null) {
            nombrarEnumsEn(operacion.getRequestBody().getContent(), version);
        }
        if (operacion.getResponses() == null) {
            return;
        }
        operacion.getResponses().forEach((codigo, respuesta) -> {
            if (codigo.startsWith("4") || codigo.startsWith("5")) {
                respuesta.setContent(new Content()
                        .addMediaType(PROBLEMA_JSON, new MediaType().schema(referencia("ProblemDetail", version))));
            } else {
                comoJson(respuesta);
                nombrarEnumsEn(respuesta.getContent(), version);
            }
        });
    }

    /** {@code *}{@code /*} pasa a {@code application/json}; los demás tipos (SSE, binario) se dejan igual. */
    private static void comoJson(ApiResponse respuesta) {
        var contenido = respuesta.getContent();
        if (contenido == null || !contenido.containsKey(TODOS)) {
            return;
        }
        var nuevo = new Content();
        contenido.forEach((tipo, medio) -> nuevo.addMediaType(TODOS.equals(tipo) ? JSON : tipo, medio));
        respuesta.setContent(nuevo);
    }

    private static void nombrarEnumsEn(Content contenido, SpecVersion version) {
        if (contenido != null) {
            contenido.values().forEach(medio -> nombrarEnums(medio.getSchema(), version, new HashSet<>()));
        }
    }

    /** Recorre el esquema y cambia cada enum en línea que coincide con uno del contrato por su referencia. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void nombrarEnums(Schema esquema, SpecVersion version, Set<Schema> vistos) {
        if (esquema == null || !vistos.add(esquema)) {
            return;
        }
        var nombre = nombreDelEnum(esquema);
        if (nombre != null) {
            aReferencia(esquema, nombre, version);
            return;
        }
        if (esquema.getProperties() != null) {
            ((Map<String, Schema>) esquema.getProperties())
                    .values()
                    .forEach(propiedad -> nombrarEnums(propiedad, version, vistos));
        }
        nombrarEnums(esquema.getItems(), version, vistos);
        if (esquema.getAdditionalProperties() instanceof Schema adicional) {
            nombrarEnums(adicional, version, vistos);
        }
        for (List<Schema> compuestos : new List[] {esquema.getAllOf(), esquema.getOneOf(), esquema.getAnyOf()}) {
            if (compuestos != null) {
                compuestos.forEach(compuesto -> nombrarEnums(compuesto, version, vistos));
            }
        }
    }

    private static String nombreDelEnum(Schema<?> esquema) {
        if (esquema.getEnum() == null || esquema.getEnum().isEmpty()) {
            return null;
        }
        var valores = new LinkedHashSet<String>();
        for (Object valor : esquema.getEnum()) {
            if (valor != null) {
                valores.add(valor.toString());
            }
        }
        return ENUMS.entrySet().stream()
                .filter(entrada -> Set.copyOf(entrada.getValue()).equals(valores))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /** Conserva la descripción. Si admitía null, en 3.1 queda {@code oneOf [referencia, null]}. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void aReferencia(Schema esquema, String nombre, SpecVersion version) {
        var nulable = Boolean.TRUE.equals(esquema.getNullable())
                || (esquema.getTypes() != null && esquema.getTypes().contains("null"));
        esquema.setEnum(null);
        esquema.setType(null);
        esquema.setTypes(null);
        esquema.setFormat(null);
        esquema.setNullable(null);
        if (!nulable) {
            esquema.set$ref(PREFIJO_REFERENCIA + nombre);
            return;
        }
        if (version == SpecVersion.V31) {
            Schema<?> nulo = new Schema<>().specVersion(SpecVersion.V31);
            nulo.addType("null");
            esquema.setOneOf(new ArrayList<>(List.of(referencia(nombre, version), nulo)));
        } else {
            esquema.setAllOf(new ArrayList<>(List.of(referencia(nombre, version))));
            esquema.setNullable(true);
        }
    }

    private static Schema<?> referencia(String nombre, SpecVersion version) {
        return new Schema<>().specVersion(version).$ref(PREFIJO_REFERENCIA + nombre);
    }

    private static Schema<?> esquemaEnum(List<String> valores, SpecVersion version) {
        var esquema = new Schema<String>().specVersion(version);
        tipo(esquema, "string", version);
        esquema.setEnum(new ArrayList<>(valores));
        return esquema;
    }

    private static Schema<?> esquemaErrorDeCampo(SpecVersion version) {
        var esquema = objeto(version).description("Un campo que no pasó la validación (GEN-001).");
        esquema.addProperty("campo", texto(version, null));
        esquema.addProperty("mensaje", texto(version, null));
        esquema.setRequired(List.of("campo", "mensaje"));
        return esquema;
    }

    /** RFC 9457 con la forma de DECISIONES-BACKEND §8 y {@code docs/api/CODIGOS-DE-ERROR.md}. */
    private static Schema<?> esquemaProblema(SpecVersion version) {
        var esquema = objeto(version).description("Error de la API (RFC 9457), con código estable en «codigo».");
        esquema.addProperty("type", texto(version, "uri"));
        esquema.addProperty("title", texto(version, null));
        var estado = new Schema<Integer>().specVersion(version).format("int32");
        tipo(estado, "integer", version);
        esquema.addProperty("status", estado);
        esquema.addProperty("detail", texto(version, null));
        esquema.addProperty("instance", texto(version, "uri"));
        esquema.addProperty("codigo", texto(version, null).description("Por ejemplo ORD-003 o GEN-001"));
        var errores = new Schema<Object>().specVersion(version).items(referencia("ErrorDeCampo", version));
        tipo(errores, "array", version);
        esquema.addProperty("errores", errores);
        esquema.addProperty("idCorrelacion", texto(version, null));
        esquema.setRequired(List.of("status"));
        return esquema;
    }

    private static Schema<?> objeto(SpecVersion version) {
        var esquema = new Schema<Object>().specVersion(version);
        tipo(esquema, "object", version);
        return esquema;
    }

    private static Schema<?> texto(SpecVersion version, String formato) {
        var esquema = new Schema<String>().specVersion(version);
        tipo(esquema, "string", version);
        if (formato != null) {
            esquema.setFormat(formato);
        }
        return esquema;
    }

    private static void tipo(Schema<?> esquema, String tipo, SpecVersion version) {
        if (version == SpecVersion.V31) {
            esquema.addType(tipo);
        } else {
            esquema.setType(tipo);
        }
    }
}
