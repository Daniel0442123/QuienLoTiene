package com.quienlotiene.arquitectura;

import com.quienlotiene.QuienLoTieneApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/** Verifica que ningún módulo use clases internas de otro (RNF-021, DECISIONES-BACKEND 4.2). */
@Tag("NF-ESC-021")
class ModulosTest {

    @Test
    @DisplayName("NF-ESC-021 Los módulos solo se comunican por su api/ o por eventos")
    void losModulosRespetanSusFronteras() {
        // Dado
        var modulos = ApplicationModules.of(QuienLoTieneApplication.class);

        // Cuando / Entonces
        modulos.verify();
    }
}
