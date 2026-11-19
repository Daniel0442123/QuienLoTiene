package com.quienlotiene.mensajeria.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.quienlotiene.mensajeria.api.MensajeEnviadoEvent;
import com.quienlotiene.mensajeria.domain.model.ChatSoloLecturaException;
import com.quienlotiene.mensajeria.domain.model.Mensaje;
import com.quienlotiene.mensajeria.domain.port.MensajeRepository;
import com.quienlotiene.ordenes.api.OrdenesApi;
import com.quienlotiene.ordenes.api.ResumenDeOrden;
import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@Tag("HU-033")
@DisplayName("HU-033 Mini chat")
class ConversarEnOrdenTest {

    private static final Instant AHORA = Instant.parse("2026-11-19T15:00:00Z");
    private static final UUID CLIENTE = UUID.randomUUID();
    private static final UUID DUENO = UUID.randomUUID();
    private static final UUID ORDEN = UUID.randomUUID();

    @Mock
    OrdenesApi ordenesApi;

    @Mock
    MensajeRepository mensajeRepository;

    @Mock
    ApplicationEventPublisher publicadorEventos;

    ConversarEnOrden conversar;

    @BeforeEach
    void configurar() {
        conversar = new ConversarEnOrden(
                ordenesApi, mensajeRepository, publicadorEventos, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private void ordenEn(String estado, boolean activa, UUID quien) {
        when(ordenesApi.obtenerResumen(ORDEN, quien))
                .thenReturn(
                        new ResumenDeOrden(ORDEN, CLIENTE, UUID.randomUUID(), DUENO, "Central", "P", estado, activa));
    }

    @Test
    @Tag("CP-MEN-033-01")
    @DisplayName("CP-MEN-033-01 El cliente escribe en una orden activa y la tienda recibe el aviso")
    void escribe() {
        // Dado
        ordenEn("PREPARANDO", true, CLIENTE);
        // Cuando
        var vista = conversar.enviar(CLIENTE, ORDEN, "  ¿Tienen bolsa?  ");
        // Entonces
        assertThat(vista.texto()).isEqualTo("¿Tienen bolsa?");
        assertThat(vista.autor()).isEqualTo("CLIENTE");
        assertThat(vista.esMio()).isTrue();
        verify(mensajeRepository).guardar(any(Mensaje.class));
        var evento = ArgumentCaptor.forClass(MensajeEnviadoEvent.class);
        verify(publicadorEventos).publishEvent(evento.capture());
        assertThat(evento.getValue().destinatarioId()).isEqualTo(DUENO);
        assertThat(evento.getValue().mensajeId()).isEqualTo(vista.id());
    }

    @Test
    @Tag("CP-MEN-033-02")
    @DisplayName("CP-MEN-033-02 Con la orden RESUELTA el chat se lee pero no se escribe (MEN-002)")
    void soloLectura() {
        ordenEn("RESUELTA", false, DUENO);
        when(mensajeRepository.listarPorOrden(ORDEN))
                .thenReturn(List.of(new Mensaje(UUID.randomUUID(), ORDEN, CLIENTE, true, "Hola", AHORA)));

        var chat = conversar.listar(DUENO, ORDEN);

        assertThat(chat.soloLectura()).isTrue();
        assertThat(chat.mensajes()).singleElement().satisfies(mensaje -> {
            assertThat(mensaje.autor()).isEqualTo("CLIENTE");
            assertThat(mensaje.esMio()).isFalse();
        });
        assertThatThrownBy(() -> conversar.enviar(DUENO, ORDEN, "Gracias"))
                .isInstanceOf(ChatSoloLecturaException.class)
                .hasFieldOrPropertyWithValue("codigo", "MEN-002");
        verify(mensajeRepository, never()).guardar(any());
        verifyNoInteractions(publicadorEventos);
    }

    @Test
    @DisplayName("Quien no participa en la orden recibe el 404 de ordenes y no lee ni escribe")
    void ordenAjena() {
        var extrano = UUID.randomUUID();
        when(ordenesApi.obtenerResumen(ORDEN, extrano)).thenThrow(new OrdenAjena());

        assertThatThrownBy(() -> conversar.listar(extrano, ORDEN)).isInstanceOf(OrdenAjena.class);
        assertThatThrownBy(() -> conversar.enviar(extrano, ORDEN, "Hola")).isInstanceOf(OrdenAjena.class);
        verifyNoInteractions(mensajeRepository, publicadorEventos);
    }

    private static final class OrdenAjena extends ReglaNegocioException {
        OrdenAjena() {
            super("ORD-001", "Orden no encontrada", "No existe.", TipoError.NO_ENCONTRADO);
        }
    }
}
