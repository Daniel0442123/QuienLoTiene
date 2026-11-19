package com.quienlotiene.tiendas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.tiendas.application.dto.TiendaPublica;
import com.quienlotiene.tiendas.domain.model.ConfiguracionDomicilio;
import com.quienlotiene.tiendas.domain.model.MedioPago;
import com.quienlotiene.tiendas.domain.model.MediosPagoAceptados;
import com.quienlotiene.tiendas.domain.model.Tienda;
import com.quienlotiene.tiendas.domain.model.TiendaNoEncontradaException;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("HU-014")
@DisplayName("HU-014 Ver detalle de tienda")
class VerDetalleTiendaTest {

    private static final Instant AHORA = Instant.parse("2026-11-19T15:00:00Z");

    @Mock
    TiendaRepository tiendaRepository;

    private Tienda registrada() {
        var tienda = Tienda.registrar(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Droguería Central",
                "3001112233",
                new Ubicacion(2.4448, -76.6147),
                AHORA);
        when(tiendaRepository.buscarPorId(tienda.getId())).thenReturn(Optional.of(tienda));
        return tienda;
    }

    @Test
    @Tag("CP-TIE-014-01")
    @DisplayName("CP-TIE-014-01 Una tienda aprobada muestra teléfono, ubicación, domicilio y medios de pago")
    void detalleDeAprobada() {
        // Dado
        var tienda = registrada();
        tienda.aprobar(AHORA);
        when(tiendaRepository.buscarDomicilio(tienda.getId())).thenReturn(ConfiguracionDomicilio.sinDomicilio());
        when(tiendaRepository.buscarMediosPago(tienda.getId()))
                .thenReturn(MediosPagoAceptados.de(List.of(MedioPago.EFECTIVO)));
        // Cuando
        var detalle = new VerDetalleTienda(tiendaRepository).ejecutar(tienda.getId());
        // Entonces
        assertThat(detalle.nombre()).isEqualTo("Droguería Central");
        assertThat(detalle.telefono()).isEqualTo("3001112233");
        assertThat(detalle.latitud()).isEqualTo(2.4448);
        assertThat(detalle.domicilio().haceDomicilio()).isFalse();
        assertThat(detalle.mediosPago()).containsExactly("EFECTIVO");
    }

    @Test
    @Tag("CP-TIE-014-02")
    @DisplayName("CP-TIE-014-02 El detalle no tiene campos de RUT, documentos, dueño ni motivo de revisión")
    void sinDatosPrivados() {
        var campos = Arrays.stream(TiendaPublica.class.getRecordComponents())
                .map(RecordComponent::getName)
                .map(String::toLowerCase)
                .toList();

        assertThat(campos)
                .noneMatch(campo -> campo.contains("rut")
                        || campo.contains("documento")
                        || campo.contains("dueno")
                        || campo.contains("motivo"));
    }

    @Test
    @DisplayName("Una tienda pendiente (o rechazada o suspendida) responde TIE-006")
    void pendienteNoSeMuestra() {
        var tienda = registrada();

        assertThatThrownBy(() -> new VerDetalleTienda(tiendaRepository).ejecutar(tienda.getId()))
                .isInstanceOf(TiendaNoEncontradaException.class);
    }
}
