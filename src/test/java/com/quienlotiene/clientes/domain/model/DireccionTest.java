package com.quienlotiene.clientes.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quienlotiene.shared.domain.Ubicacion;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-007")
@DisplayName("HU-007 Dirección del cliente")
class DireccionTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:30:00Z");
    private static final UUID CLIENTE = UUID.randomUUID();
    private static final Ubicacion CASA = new Ubicacion(2.4448, -76.6147);

    private static Direccion casa(Ubicacion ubicacion, String indicaciones) {
        return Direccion.registrar(
                UUID.randomUUID(),
                CLIENTE,
                " Casa ",
                ubicacion,
                " Calle 5 # 10-20 ",
                indicaciones,
                "3001234567",
                AHORA);
    }

    @Test
    @Tag("CP-CLI-007-01")
    @DisplayName("CP-CLI-007-01 Guarda \"Casa\" con pin, dirección escrita, indicaciones y teléfono")
    void guardaLaDireccion() {
        var direccion = casa(CASA, " Portón verde ");

        assertThat(direccion.getNombre()).isEqualTo("Casa");
        assertThat(direccion.getUbicacion()).isEqualTo(CASA);
        assertThat(direccion.getDireccionEscrita()).isEqualTo("Calle 5 # 10-20");
        assertThat(direccion.getIndicaciones()).isEqualTo("Portón verde");
        assertThat(direccion.getTelefono()).isEqualTo("3001234567");
        assertThat(direccion.getFechaCreacion()).isEqualTo(AHORA);
        assertThat(direccion.esDe(CLIENTE)).isTrue();
        assertThat(direccion.esDe(UUID.randomUUID())).isFalse();
        assertThat(casa(CASA, "  ").getIndicaciones()).isNull();
    }

    @Test
    @Tag("CP-CLI-007-02")
    @DisplayName("CP-CLI-007-02 Sin ubicación en el mapa se rechaza e indica el dato faltante")
    void rechazaLaDireccionSinUbicacion() {
        assertThatThrownBy(() -> casa(null, null))
                .isInstanceOf(DireccionIncompletaException.class)
                .hasMessageContaining("ubicación en el mapa")
                .extracting("codigo")
                .isEqualTo("CLI-001");
    }

    @Test
    @DisplayName("Rechaza nombre, dirección o teléfono vacíos y textos muy largos")
    void rechazaDatosInvalidos() {
        var direccion = casa(CASA, null);

        assertThatThrownBy(() -> direccion.cambiar(" ", CASA, "Calle 5", null, "300"))
                .hasMessageContaining("nombre");
        assertThatThrownBy(() -> direccion.cambiar("Casa", CASA, null, null, "300"))
                .hasMessageContaining("dirección escrita");
        assertThatThrownBy(() -> direccion.cambiar("Casa", CASA, "Calle 5", null, ""))
                .hasMessageContaining("teléfono");
        assertThatThrownBy(() -> direccion.cambiar("x".repeat(41), CASA, "Calle 5", null, "300"))
                .hasMessageContaining("40");
        assertThatThrownBy(() -> direccion.cambiar("Casa", CASA, "Calle 5", "x".repeat(201), "300"))
                .hasMessageContaining("indicaciones");
        assertThat(direccion.getNombre()).isEqualTo("Casa");
    }

    @Test
    @DisplayName("Editar cambia todos los datos")
    void editaLaDireccion() {
        var direccion = casa(CASA, null);
        var oficina = new Ubicacion(2.45, -76.6);

        direccion.cambiar("Oficina", oficina, "Carrera 9 # 4-10", "Piso 3", "3109876543");

        assertThat(direccion.getNombre()).isEqualTo("Oficina");
        assertThat(direccion.getUbicacion()).isEqualTo(oficina);
        assertThat(direccion.getIndicaciones()).isEqualTo("Piso 3");
    }
}
