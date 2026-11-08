package com.quienlotiene.solicitudes.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.shared.domain.UbicacionInvalidaException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("HU-016")
@DisplayName("HU-016 Solicitud")
class SolicitudTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:30:00Z");
    private static final Duration TREINTA_MINUTOS = Duration.ofMinutes(30);
    private static final Ubicacion POPAYAN = new Ubicacion(2.4448, -76.6147);
    private static final UUID FARMACIAS = UUID.randomUUID();

    private static Solicitud publicar(String producto, int cantidad, Ubicacion ubicacion, Modalidad modalidad) {
        return Solicitud.publicar(
                UUID.randomUUID(),
                UUID.randomUUID(),
                FARMACIAS,
                producto,
                cantidad,
                modalidad,
                false,
                ubicacion,
                3_000,
                TREINTA_MINUTOS,
                AHORA);
    }

    @Test
    @Tag("CP-SOL-016-01")
    @DisplayName("CP-SOL-016-01 Queda ABIERTA con vencimiento a los 30 minutos y radio de 3 km")
    void quedaAbiertaConSuVencimiento() {
        // Cuando
        var solicitud = publicar("  Paracetamol 500 mg ", 1, POPAYAN, Modalidad.RECOGER);

        // Entonces
        assertThat(solicitud.getEstado()).isEqualTo(EstadoSolicitud.ABIERTA);
        assertThat(solicitud.getFechaPublicacion()).isEqualTo(AHORA);
        assertThat(solicitud.getFechaVencimiento()).isEqualTo(AHORA.plus(TREINTA_MINUTOS));
        assertThat(solicitud.getRadioEnMetros()).isEqualTo(3_000);
        assertThat(solicitud.getProducto()).isEqualTo("Paracetamol 500 mg");
        assertThat(solicitud.getCategoriaId()).isEqualTo(FARMACIAS);
        assertThat(solicitud.getModalidad()).isEqualTo(Modalidad.RECOGER);
        assertThat(solicitud.esUrgente()).isFalse();
        assertThat(solicitud.getUbicacion()).isEqualTo(POPAYAN);
        assertThat(solicitud.getCantidad()).isEqualTo(1);
        assertThat(solicitud.getId()).isNotNull();
        assertThat(solicitud.getClienteId()).isNotNull();
    }

    @Test
    @DisplayName("Vence justo a los 30 minutos")
    void venceALosTreintaMinutos() {
        var solicitud = publicar("Paracetamol", 1, POPAYAN, Modalidad.RECOGER);

        assertThat(solicitud.estaVencida(AHORA.plus(TREINTA_MINUTOS).minusSeconds(1)))
                .isFalse();
        assertThat(solicitud.estaVencida(AHORA.plus(TREINTA_MINUTOS))).isTrue();
    }

    @ParameterizedTest
    @Tag("CP-SOL-016-02")
    @ValueSource(strings = {"", "   "})
    @DisplayName("CP-SOL-016-02 Sin producto se rechaza e indica el dato faltante")
    void rechazaLaSolicitudSinProducto(String productoVacio) {
        assertThatThrownBy(() -> publicar(productoVacio, 1, POPAYAN, Modalidad.RECOGER))
                .isInstanceOf(SolicitudIncompletaException.class)
                .hasMessageContaining("producto")
                .extracting("codigo")
                .isEqualTo("SOL-002");
        assertThatThrownBy(() -> publicar(null, 1, POPAYAN, Modalidad.RECOGER))
                .isInstanceOf(SolicitudIncompletaException.class);
    }

    @Test
    @Tag("CP-SOL-016-02")
    @DisplayName("CP-SOL-016-02 Sin ubicación se rechaza e indica el dato faltante")
    void rechazaLaSolicitudSinUbicacion() {
        assertThatThrownBy(() -> publicar("Paracetamol", 1, null, Modalidad.RECOGER))
                .isInstanceOf(SolicitudIncompletaException.class)
                .hasMessageContaining("ubicación");
    }

    @Test
    @Tag("CP-SOL-016-02")
    @DisplayName("Sin categoría o sin modalidad se rechaza")
    void rechazaLaSolicitudSinCategoriaOModalidad() {
        assertThatThrownBy(() -> publicar("Paracetamol", 1, POPAYAN, null))
                .isInstanceOf(SolicitudIncompletaException.class)
                .hasMessageContaining("recoger o quieres domicilio");
        assertThatThrownBy(() -> Solicitud.publicar(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "Paracetamol",
                        1,
                        Modalidad.DOMICILIO,
                        true,
                        POPAYAN,
                        3_000,
                        TREINTA_MINUTOS,
                        AHORA))
                .isInstanceOf(SolicitudIncompletaException.class)
                .hasMessageContaining("categoría");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 1000})
    @DisplayName("La cantidad debe estar entre 1 y 999")
    void rechazaUnaCantidadFueraDeRango(int cantidadInvalida) {
        assertThatThrownBy(() -> publicar("Paracetamol", cantidadInvalida, POPAYAN, Modalidad.RECOGER))
                .isInstanceOf(SolicitudIncompletaException.class)
                .hasMessageContaining("cantidad");
    }

    @Test
    @DisplayName("Acepta 150 caracteres en el producto y rechaza 151")
    void limitaLaLongitudDelProducto() {
        assertThat(publicar("a".repeat(150), 1, POPAYAN, Modalidad.RECOGER).getProducto())
                .hasSize(150);
        assertThatThrownBy(() -> publicar("a".repeat(151), 1, POPAYAN, Modalidad.RECOGER))
                .isInstanceOf(SolicitudIncompletaException.class);
    }

    @Test
    @DisplayName("Una ubicación fuera del planeta se rechaza")
    void rechazaUnaUbicacionInvalida() {
        assertThatThrownBy(() -> new Ubicacion(91, 0)).isInstanceOf(UbicacionInvalidaException.class);
        assertThatThrownBy(() -> new Ubicacion(0, -181)).isInstanceOf(UbicacionInvalidaException.class);
        assertThatThrownBy(() -> new Ubicacion(Double.NaN, 0)).isInstanceOf(UbicacionInvalidaException.class);
        assertThatThrownBy(() -> new Ubicacion(0, Double.NaN))
                .isInstanceOf(UbicacionInvalidaException.class)
                .extracting("codigo")
                .isEqualTo("GEN-009");
    }
}
