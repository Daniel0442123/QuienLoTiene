package com.quienlotiene.solicitudes.application.usecase;

import static com.quienlotiene.solicitudes.domain.model.SolicitudesDePrueba.AHORA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.quienlotiene.solicitudes.application.dto.RespuestasDeSolicitud;
import com.quienlotiene.solicitudes.domain.model.EnvioSolicitud;
import com.quienlotiene.solicitudes.domain.model.Modalidad;
import com.quienlotiene.solicitudes.domain.model.Respuesta;
import com.quienlotiene.solicitudes.domain.model.SolicitudNoEncontradaException;
import com.quienlotiene.solicitudes.domain.model.SolicitudParaTienda;
import com.quienlotiene.solicitudes.domain.model.SolicitudesDePrueba;
import com.quienlotiene.solicitudes.domain.model.TipoRespuesta;
import com.quienlotiene.solicitudes.domain.port.EnvioSolicitudRepository;
import com.quienlotiene.solicitudes.domain.port.RespuestaRepository;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import com.quienlotiene.tiendas.api.DatosTienda;
import com.quienlotiene.tiendas.api.DatosTiendasApi;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
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
@DisplayName("HU-018, HU-020 y HU-021 Consultas de solicitudes y respuestas")
class ConsultasDeRespuestasTest {

    private static final UUID CLIENTE = UUID.randomUUID();
    private static final UUID DUENO = UUID.randomUUID();
    private static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);

    @Mock
    DatosTiendasApi datosTiendasApi;

    @Mock
    SolicitudRepository solicitudRepository;

    @Mock
    EnvioSolicitudRepository envioRepository;

    @Mock
    RespuestaRepository respuestaRepository;

    @Test
    @Tag("HU-018")
    @Tag("CP-SOL-018-01")
    @Tag("CP-SOL-018-02")
    @DisplayName("CP-SOL-018-01 La tienda ve sus solicitudes sin datos del cliente y con la distancia redondeada")
    void listaDisponibles() {
        // Dado
        var tienda = new DatosTienda(UUID.randomUUID(), DUENO, "Droguería", "3001112233");
        var solicitud = SolicitudesDePrueba.abierta();
        when(datosTiendasApi.obtenerTiendaDelDueno(DUENO)).thenReturn(tienda);
        when(solicitudRepository.listarDisponiblesParaTienda(tienda.tiendaId(), AHORA))
                .thenReturn(List.of(new SolicitudParaTienda(solicitud, 1_234)));
        when(respuestaRepository.buscarUltimoPrecio(tienda.tiendaId(), "Paracetamol 500 mg"))
                .thenReturn(Optional.of(7_800L));
        // Cuando
        var disponibles = new ListarSolicitudesDisponibles(
                        datosTiendasApi, solicitudRepository, respuestaRepository, RELOJ)
                .ejecutar(DUENO);
        // Entonces
        assertThat(disponibles).singleElement().satisfies(disponible -> {
            assertThat(disponible.id()).isEqualTo(solicitud.getId());
            assertThat(disponible.distanciaEnMetros()).isEqualTo(1_200);
            assertThat(disponible.precioSugeridoEnPesos()).isEqualTo(7_800L);
            assertThat(disponible.modalidad()).isEqualTo("DOMICILIO");
        });
        assertThat(disponibles.get(0).getClass().getRecordComponents())
                .extracting(componente -> componente.getName())
                .doesNotContain("clienteId", "latitud", "longitud", "telefono", "nombreCliente");
    }

    @Test
    @Tag("HU-021")
    @DisplayName("Mis respuestas de las últimas 24 horas con el estado de cada una")
    void listaMisRespuestas() {
        var tienda = new DatosTienda(UUID.randomUUID(), DUENO, "Droguería", "3001112233");
        var solicitud = SolicitudesDePrueba.abierta();
        var respuesta = SolicitudesDePrueba.si(solicitud, tienda.tiendaId(), 8_000, 2);
        var huerfana = SolicitudesDePrueba.si(SolicitudesDePrueba.abierta(), tienda.tiendaId(), 1, 1);
        when(datosTiendasApi.obtenerTiendaDelDueno(DUENO)).thenReturn(tienda);
        when(respuestaRepository.listarConProductoDeTiendaDesde(tienda.tiendaId(), AHORA.minus(Duration.ofHours(24))))
                .thenReturn(List.of(respuesta, huerfana));
        when(solicitudRepository.buscarPorId(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.buscarPorId(huerfana.getSolicitudId())).thenReturn(Optional.empty());

        var mias = new ListarMisRespuestas(datosTiendasApi, respuestaRepository, solicitudRepository, RELOJ)
                .ejecutar(DUENO);

        assertThat(mias).singleElement().satisfies(mia -> {
            assertThat(mia.estado()).isEqualTo("ESPERANDO");
            assertThat(mia.cantidadSolicitada()).isEqualTo(2);
            assertThat(mia.precioEnPesos()).isEqualTo(8_000L);
        });
    }

    @Test
    @Tag("HU-020")
    @Tag("HU-021")
    @DisplayName("El cliente consulta su solicitud con tiendas notificadas, respuestas y la orden si eligió")
    void consultaSolicitud() {
        var solicitud = SolicitudesDePrueba.abierta(CLIENTE, Modalidad.DOMICILIO);
        var elegida = SolicitudesDePrueba.si(solicitud, UUID.randomUUID(), 8_000, 2);
        var orden = UUID.randomUUID();
        elegida.elegir(orden);
        when(solicitudRepository.buscarPorId(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(respuestaRepository.listarDe(solicitud.getId()))
                .thenReturn(List.of(SolicitudesDePrueba.no(solicitud, UUID.randomUUID()), elegida));
        when(envioRepository.listarDe(solicitud.getId()))
                .thenReturn(List.of(envio(solicitud.getId(), UUID.randomUUID(), 300)));
        var consultar = new ConsultarSolicitud(solicitudRepository, envioRepository, respuestaRepository);

        var vista = consultar.ejecutar(CLIENTE, solicitud.getId());

        assertThat(vista.respuestasRecibidas()).isEqualTo(2);
        assertThat(vista.tiendasNotificadas()).isEqualTo(1);
        assertThat(vista.ordenId()).isEqualTo(orden);
        assertThat(vista.estado()).isEqualTo("ABIERTA");
        assertThat(vista.toString()).doesNotContain("Paracetamol");
        assertThatThrownBy(() -> consultar.ejecutar(UUID.randomUUID(), solicitud.getId()))
                .isInstanceOf(SolicitudNoEncontradaException.class);
    }

    @Test
    @Tag("HU-020")
    @Tag("CP-SOL-020-02")
    @Tag("CP-SOL-020-03")
    @DisplayName("CP-SOL-020-02 Las tarjetas salen ordenadas por precio, sin las NO y con la alternativa marcada")
    void listaTarjetasOrdenadas() {
        // Dado
        var solicitud = SolicitudesDePrueba.abierta(CLIENTE, Modalidad.DOMICILIO);
        var tiendaA = UUID.randomUUID();
        var tiendaB = UUID.randomUUID();
        var tiendaC = UUID.randomUUID();
        var cara = SolicitudesDePrueba.si(solicitud, tiendaA, 9_000, 2).conCostoDomicilio(2_000L);
        var barata = Respuesta.responder(
                UUID.randomUUID(),
                solicitud,
                tiendaB,
                UUID.randomUUID(),
                TipoRespuesta.PARECIDO,
                7_500L,
                2,
                "Genérico",
                null,
                null,
                AHORA);
        var media = SolicitudesDePrueba.si(solicitud, tiendaC, 8_200, 1);
        var no = SolicitudesDePrueba.no(solicitud, UUID.randomUUID());
        when(solicitudRepository.buscarPorId(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(respuestaRepository.listarDe(solicitud.getId())).thenReturn(List.of(cara, barata, media, no));
        when(envioRepository.listarDe(solicitud.getId()))
                .thenReturn(List.of(envio(solicitud.getId(), tiendaA, 400), envio(solicitud.getId(), tiendaB, 1_100)));
        when(datosTiendasApi.buscarPorIds(any()))
                .thenReturn(List.of(new DatosTienda(tiendaA, UUID.randomUUID(), "Droguería A", "3000000000")));
        var listar = new ListarRespuestasDeSolicitud(
                solicitudRepository, respuestaRepository, envioRepository, datosTiendasApi);
        // Cuando
        var porPrecio = listar.ejecutar(CLIENTE, solicitud.getId(), "precio");
        var porTotal = listar.ejecutar(CLIENTE, solicitud.getId(), "total");
        // Entonces
        assertThat(porPrecio.respuestasRecibidas()).isEqualTo(4);
        assertThat(porPrecio.respuestas())
                .extracting(RespuestasDeSolicitud.Tarjeta::precioEnPesos)
                .containsExactly(7_500L, 8_200L, 9_000L);
        var primera = porPrecio.respuestas().get(0);
        assertThat(primera.esAlternativa()).isTrue();
        assertThat(primera.nombreTienda()).isEqualTo("Tienda");
        assertThat(primera.distanciaEnMetros()).isEqualTo(1_100);
        var ultima = porPrecio.respuestas().get(2);
        assertThat(ultima.nombreTienda()).isEqualTo("Droguería A");
        assertThat(ultima.totalEnPesos()).isEqualTo(20_000);
        assertThat(porTotal.respuestas())
                .extracting(RespuestasDeSolicitud.Tarjeta::totalEnPesos)
                .containsExactly(8_200L, 15_000L, 20_000L);
        assertThatThrownBy(() -> listar.ejecutar(UUID.randomUUID(), solicitud.getId(), null))
                .isInstanceOf(SolicitudNoEncontradaException.class);
    }

    private static EnvioSolicitud envio(UUID solicitudId, UUID tiendaId, int distancia) {
        return new EnvioSolicitud(UUID.randomUUID(), solicitudId, tiendaId, UUID.randomUUID(), distancia, AHORA);
    }
}
