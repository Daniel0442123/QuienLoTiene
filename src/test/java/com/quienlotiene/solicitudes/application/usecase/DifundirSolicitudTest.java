package com.quienlotiene.solicitudes.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.solicitudes.api.SolicitudDifundidaEvent;
import com.quienlotiene.solicitudes.domain.model.EnvioSolicitud;
import com.quienlotiene.solicitudes.domain.model.Modalidad;
import com.quienlotiene.solicitudes.domain.model.Solicitud;
import com.quienlotiene.solicitudes.domain.port.EnvioSolicitudRepository;
import com.quienlotiene.tiendas.api.TiendaElegible;
import com.quienlotiene.tiendas.api.TiendasApi;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@Tag("HU-017")
@DisplayName("HU-017 Difundir la solicitud a las tiendas elegibles")
class DifundirSolicitudTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:30:00Z");
    private static final UUID FARMACIAS = UUID.randomUUID();

    @Mock
    TiendasApi tiendasApi;

    @Mock
    EnvioSolicitudRepository envioRepository;

    @Mock
    ApplicationEventPublisher publicadorEventos;

    private static Solicitud paracetamol() {
        return Solicitud.publicar(
                UUID.randomUUID(),
                UUID.randomUUID(),
                FARMACIAS,
                "Paracetamol 500 mg",
                1,
                Modalidad.RECOGER,
                false,
                new Ubicacion(2.4448, -76.6147),
                3_000,
                Duration.ofMinutes(30),
                AHORA);
    }

    @Test
    @Tag("CP-SOL-017-01")
    @DisplayName("CP-SOL-017-01 Busca en el radio de 3 km de la categoría, guarda a quién llegó y avisa a cada dueño")
    void difundeALasTiendasElegibles() {
        // Dado: tiendas responde las farmacias a 1 km y 2 km
        var solicitud = paracetamol();
        var aUnKilometro = new TiendaElegible(UUID.randomUUID(), UUID.randomUUID(), "La 5", 1_000);
        var aDosKilometros = new TiendaElegible(UUID.randomUUID(), UUID.randomUUID(), "La 9", 2_000);
        when(tiendasApi.buscarElegibles(FARMACIAS, 2.4448, -76.6147, 3_000, AHORA))
                .thenReturn(List.of(aUnKilometro, aDosKilometros));

        // Cuando
        var tiendasNotificadas =
                new DifundirSolicitud(tiendasApi, envioRepository, publicadorEventos).difundir(solicitud, AHORA);

        // Entonces
        assertThat(tiendasNotificadas).isEqualTo(2);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EnvioSolicitud>> envios = ArgumentCaptor.forClass(List.class);
        verify(envioRepository).guardarTodos(envios.capture());
        assertThat(envios.getValue())
                .extracting(EnvioSolicitud::tiendaId)
                .containsExactly(aUnKilometro.tiendaId(), aDosKilometros.tiendaId());
        assertThat(envios.getValue()).allMatch(envio -> envio.solicitudId().equals(solicitud.getId()));
        var evento = ArgumentCaptor.forClass(SolicitudDifundidaEvent.class);
        verify(publicadorEventos).publishEvent(evento.capture());
        assertThat(evento.getValue().destinatarios())
                .extracting(SolicitudDifundidaEvent.Destinatario::duenoId)
                .containsExactly(aUnKilometro.duenoId(), aDosKilometros.duenoId());
    }

    @Test
    @DisplayName("Sin tiendas elegibles la solicitud queda publicada y el evento va sin destinatarios")
    void sinTiendasElegibles() {
        var solicitud = paracetamol();
        when(tiendasApi.buscarElegibles(FARMACIAS, 2.4448, -76.6147, 3_000, AHORA))
                .thenReturn(List.of());

        var tiendasNotificadas =
                new DifundirSolicitud(tiendasApi, envioRepository, publicadorEventos).difundir(solicitud, AHORA);

        assertThat(tiendasNotificadas).isZero();
        verify(envioRepository).guardarTodos(List.of());
    }
}
