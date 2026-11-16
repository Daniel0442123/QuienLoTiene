package com.quienlotiene.shared.infrastructure.web;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.media.Schema;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * DEF-001: springdoc no deduce qué campos de un {@code record} son obligatorios. Este convertidor marca como
 * {@code required} todos los componentes del record y, si el componente lleva {@link Nullable}, además lo declara
 * {@code nullable} (llega como {@code null} cuando no aplica). Los {@code *Request} se dejan como están: ahí lo obligatorio
 * lo dice Bean Validation. Así el frontend genera sus tipos desde el contrato.
 */
public class ConvertidorRecordsOpenApi implements ModelConverter {

    private static final String PREFIJO_REFERENCIA = "#/components/schemas/";

    @Override
    public Schema<?> resolve(AnnotatedType tipo, ModelConverterContext contexto, Iterator<ModelConverter> cadena) {
        Schema<?> esquema = cadena.hasNext() ? cadena.next().resolve(tipo, contexto, cadena) : null;
        if (esquema == null || tipo.getType() == null) {
            return esquema;
        }
        var clase = Json.mapper().constructType(tipo.getType()).getRawClass();
        // Los Request ya declaran lo obligatorio con Bean Validation (@NotNull, @NotBlank)
        if (!clase.isRecord() || clase.getSimpleName().endsWith("Request")) {
            return esquema;
        }
        var definicion = definicionDe(esquema, contexto);
        if (definicion != null && definicion.getProperties() != null) {
            completar(definicion, clase.getRecordComponents());
        }
        return esquema;
    }

    private static Schema<?> definicionDe(Schema<?> esquema, ModelConverterContext contexto) {
        if (esquema.get$ref() == null) {
            return esquema;
        }
        var nombre = esquema.get$ref().replace(PREFIJO_REFERENCIA, "");
        return contexto.getDefinedModels().get(nombre);
    }

    private static void completar(Schema<?> definicion, RecordComponent[] componentes) {
        Set<String> obligatorios = new LinkedHashSet<>();
        if (definicion.getRequired() != null) {
            obligatorios.addAll(definicion.getRequired());
        }
        for (var componente : componentes) {
            var nombre = componente.getName();
            Schema<?> propiedad = definicion.getProperties().get(nombre);
            if (propiedad == null) {
                continue;
            }
            obligatorios.add(nombre);
            if (esNulable(componente)) {
                marcarNulable(propiedad);
            }
        }
        definicion.setRequired(new ArrayList<>(obligatorios));
    }

    private static boolean esNulable(RecordComponent componente) {
        return componente.isAnnotationPresent(Nullable.class)
                || componente.getAnnotatedType().isAnnotationPresent(Nullable.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void marcarNulable(Schema propiedad) {
        if (propiedad.getSpecVersion() == SpecVersion.V31) {
            if (propiedad.get$ref() != null) {
                // En 3.1 una referencia nulable se escribe como oneOf [referencia, null]
                var referencia = new Schema<>().$ref(propiedad.get$ref());
                propiedad.set$ref(null);
                propiedad.addOneOfItem(referencia);
                Schema<?> nulo = new Schema<>().specVersion(SpecVersion.V31);
                nulo.addType("null");
                propiedad.addOneOfItem(nulo);
                return;
            }
            var tipos = propiedad.getTypes() == null
                    ? new LinkedHashSet<String>()
                    : new LinkedHashSet<>(propiedad.getTypes());
            if (tipos.isEmpty() && propiedad.getType() != null) {
                tipos.add(propiedad.getType());
            }
            tipos.add("null");
            propiedad.setTypes(tipos);
        } else {
            propiedad.setNullable(true);
        }
    }
}
