package com.quienlotiene.shared.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("DEF-001")
@Tag("NF-MAN-024")
class ConvertidorRecordsOpenApiTest {

    record Interno(String calle) {}

    record Ejemplo(
            String obligatorio,
            int numero,
            @Nullable String opcional,
            @Nullable Interno interno,
            Interno fijo) {}

    @Test
    @DisplayName("DEF-001 Todos los componentes del record son required y los @Nullable además nullable")
    void marcaObligatoriosYNulables() {
        // Dado
        var convertidores = new ModelConverters(false);
        convertidores.addConverter(new ConvertidorRecordsOpenApi());

        // Cuando
        Map<String, Schema> esquemas = convertidores.readAll(Ejemplo.class);

        // Entonces
        var ejemplo = esquemas.get("Ejemplo");
        assertThat(ejemplo.getRequired())
                .containsExactlyInAnyOrder("obligatorio", "numero", "opcional", "interno", "fijo");
        var opcional = (Schema<?>) ejemplo.getProperties().get("opcional");
        assertThat(Boolean.TRUE.equals(opcional.getNullable())
                        || (opcional.getTypes() != null && opcional.getTypes().contains("null")))
                .isTrue();
        var obligatorio = (Schema<?>) ejemplo.getProperties().get("obligatorio");
        assertThat(obligatorio.getNullable()).isNotEqualTo(Boolean.TRUE);
        assertThat(esquemas.get("Interno").getRequired()).containsExactly("calle");
    }
}
