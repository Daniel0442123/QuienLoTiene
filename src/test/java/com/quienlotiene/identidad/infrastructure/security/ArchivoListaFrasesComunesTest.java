package com.quienlotiene.identidad.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.quienlotiene.identidad.domain.model.FraseContrasena;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-001")
@Tag("CP-IDE-001-03")
@DisplayName("HU-001 Lista de frases comunes")
class ArchivoListaFrasesComunesTest {

    private final ArchivoListaFrasesComunes lista = new ArchivoListaFrasesComunes();

    @Test
    @DisplayName("CP-IDE-001-03 Reconoce una contraseña filtrada sin importar mayúsculas")
    void reconoceUnaFraseFiltrada() {
        assertThat(lista.contiene(new FraseContrasena("PasswordPassword"))).isTrue();
        assertThat(lista.contiene(new FraseContrasena("1q2w3e4r5t6y7u8i"))).isTrue();
    }

    @Test
    @DisplayName("Deja pasar una frase propia")
    void dejaPasarUnaFrasePropia() {
        assertThat(lista.contiene(new FraseContrasena("mi perro come mango los domingos")))
                .isFalse();
    }

    @Test
    @DisplayName("Ignora los comentarios del archivo")
    void ignoraLosComentarios() {
        assertThat(lista.cantidadFrases()).isGreaterThan(50);
        assertThat(lista.contiene(new FraseContrasena("# Contraseñas comunes o filtradas de 15 o más caracteres")))
                .isFalse();
    }
}
