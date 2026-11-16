package com.quienlotiene.mensajeria.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-032")
@DisplayName("HU-032 Enlace de WhatsApp")
class EnlaceWhatsappTest {

    @Test
    @Tag("CP-MEN-032-01")
    @DisplayName(
            "CP-MEN-032-01 Un celular colombiano escrito con espacios queda con el indicativo 57 y el mensaje codificado")
    void armaElEnlace() {
        // Dado
        var orden = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000000");
        // Cuando
        var enlace = EnlaceWhatsapp.para("300 111 2233", orden, "Paracetamol 500 mg");
        // Entonces
        assertThat(enlace.telefono()).isEqualTo("573001112233");
        assertThat(enlace.enlace())
                .startsWith("https://wa.me/573001112233?text=")
                .contains("A1B2C3D4")
                .contains("Paracetamol%20500%20mg")
                .doesNotContain("+")
                .doesNotContain(" ");
    }

    @Test
    @DisplayName("Un número que ya trae indicativo o no es celular se deja solo con sus dígitos")
    void normaliza() {
        assertThat(EnlaceWhatsapp.normalizar("+57 300-111-2233")).isEqualTo("573001112233");
        assertThat(EnlaceWhatsapp.normalizar("(602) 8201234")).isEqualTo("6028201234");
        assertThat(EnlaceWhatsapp.normalizar(null)).isEmpty();
        assertThat(EnlaceWhatsapp.numeroCorto(UUID.fromString("abcdef12-3456-7890-abcd-ef1234567890")))
                .isEqualTo("ABCDEF12");
    }
}
