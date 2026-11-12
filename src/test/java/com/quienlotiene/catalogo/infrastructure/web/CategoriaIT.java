package com.quienlotiene.catalogo.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.ContenedoresDePrueba;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Las categorías sembradas por la migración se leen sin sesión, como las pide el registro de tienda. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@Tag("HU-002")
@Tag("HU-016")
@DisplayName("HU-002 y HU-016 Categorías por la API")
class CategoriaIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("Lista Farmacia y Ferretería sin pedir sesión")
    void listaLasCategoriasSembradasSinSesion() throws Exception {
        // Dado: la migración del módulo catalogo sembró las categorías del MVP

        // Cuando / Entonces
        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("3f6b1c2a-0001-4a8e-9b1d-5c2e7a9f0001"))
                .andExpect(jsonPath("$[0].nombre").value("Farmacia"))
                .andExpect(jsonPath("$[1].id").value("3f6b1c2a-0002-4a8e-9b1d-5c2e7a9f0002"))
                .andExpect(jsonPath("$[1].nombre").value("Ferretería"));
    }
}
