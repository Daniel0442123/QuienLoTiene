package com.quienlotiene.identidad.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.quienlotiene.identidad.application.dto.IniciarSesionComando;
import com.quienlotiene.identidad.application.dto.SesionIniciada;
import com.quienlotiene.identidad.domain.model.Correo;
import com.quienlotiene.identidad.domain.model.CredencialesIncorrectasException;
import com.quienlotiene.identidad.domain.model.CuentaSuspendidaException;
import com.quienlotiene.identidad.domain.model.EstadoCuenta;
import com.quienlotiene.identidad.domain.model.Rol;
import com.quienlotiene.identidad.domain.model.SegundoFactorRequeridoException;
import com.quienlotiene.identidad.domain.model.SesionVencidaException;
import com.quienlotiene.identidad.domain.model.TokenAcceso;
import com.quienlotiene.identidad.domain.model.TokenRefresco;
import com.quienlotiene.identidad.domain.model.Usuario;
import com.quienlotiene.identidad.domain.model.ValorTokenOpaco;
import com.quienlotiene.identidad.domain.port.CodificadorFrasesContrasena;
import com.quienlotiene.identidad.domain.port.DesafioSegundoFactorRepository;
import com.quienlotiene.identidad.domain.port.GeneradorTokenAcceso;
import com.quienlotiene.identidad.domain.port.TokenRefrescoRepository;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("HU-003")
@DisplayName("HU-003 Iniciar, renovar y cerrar sesión")
class SesionesTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:30:00Z");
    private static final Duration SIETE_DIAS = Duration.ofDays(7);
    private static final String FRASE = "mi perro come mango los domingos";
    private static final String HASH = "$argon2id$hash";

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    CodificadorFrasesContrasena codificadorFrases;

    @Mock
    GeneradorTokenAcceso generadorTokenAcceso;

    @Mock
    TokenRefrescoRepository tokenRefrescoRepository;

    @Mock
    DesafioSegundoFactorRepository desafioRepository;

    Clock reloj = Clock.fixed(AHORA, ZoneOffset.UTC);
    EmisorSesion emisorSesion;

    @BeforeEach
    void configurar() {
        emisorSesion = new EmisorSesion(generadorTokenAcceso, tokenRefrescoRepository, () -> SIETE_DIAS);
    }

    private static Usuario usuario(Rol rol, EstadoCuenta estado) {
        return Usuario.reconstituir(
                UUID.randomUUID(), "Ana Pérez", new Correo("ana@correo.com"), "3001234567", HASH, rol, estado, AHORA);
    }

    @Nested
    @DisplayName("Iniciar sesión")
    class IniciarSesionTest {

        IniciarSesion iniciarSesion;

        @BeforeEach
        void configurar() {
            iniciarSesion =
                    new IniciarSesion(usuarioRepository, codificadorFrases, emisorSesion, desafioRepository, reloj);
        }

        @Test
        @Tag("CP-IDE-003-01")
        @DisplayName("CP-IDE-003-01 Con credenciales correctas entrega token de 15 minutos y token de refresco")
        void entregaLosTokensConCredencialesCorrectas() {
            // Dado: un cliente con cuenta activa
            var cliente = usuario(Rol.CLIENTE, EstadoCuenta.ACTIVA);
            when(usuarioRepository.buscarPorCorreo(new Correo("ana@correo.com")))
                    .thenReturn(Optional.of(cliente));
            when(codificadorFrases.coincide(FRASE, HASH)).thenReturn(true);
            when(generadorTokenAcceso.generar(cliente.getId(), Rol.CLIENTE))
                    .thenReturn(new TokenAcceso("jwt-firmado", AHORA.plus(Duration.ofMinutes(15))));

            // Cuando
            var sesion = (SesionIniciada) iniciarSesion.ejecutar(new IniciarSesionComando("Ana@Correo.com", FRASE));

            // Entonces
            assertThat(sesion.tokenAcceso()).isEqualTo("jwt-firmado");
            assertThat(sesion.fechaExpiracionAcceso()).isEqualTo(AHORA.plus(Duration.ofMinutes(15)));
            assertThat(sesion.rol()).isEqualTo("CLIENTE");
            assertThat(sesion.fechaExpiracionRefresco()).isEqualTo(AHORA.plus(SIETE_DIAS));
            var tokenGuardado = ArgumentCaptor.forClass(TokenRefresco.class);
            verify(tokenRefrescoRepository).guardar(tokenGuardado.capture());
            assertThat(tokenGuardado.getValue().getUsuarioId()).isEqualTo(cliente.getId());
            assertThat(tokenGuardado.getValue().getHashToken())
                    .isEqualTo(new ValorTokenOpaco(sesion.tokenRefresco()).calcularHash());
        }

        @Test
        @Tag("CP-IDE-003-02")
        @DisplayName("CP-IDE-003-02 Con la frase incorrecta niega el acceso con un mensaje genérico")
        void niegaElAccesoConFraseIncorrecta() {
            // Dado
            var cliente = usuario(Rol.CLIENTE, EstadoCuenta.ACTIVA);
            when(usuarioRepository.buscarPorCorreo(any())).thenReturn(Optional.of(cliente));
            when(codificadorFrases.coincide("frase equivocada cualquiera", HASH))
                    .thenReturn(false);

            // Cuando / Entonces
            assertThatThrownBy(() -> iniciarSesion.ejecutar(
                            new IniciarSesionComando("ana@correo.com", "frase equivocada cualquiera")))
                    .isInstanceOf(CredencialesIncorrectasException.class)
                    .hasMessage("El correo o la frase de contraseña no son correctos.");
            verifyNoInteractions(tokenRefrescoRepository);
        }

        @Test
        @Tag("CP-IDE-003-02")
        @DisplayName("CP-IDE-003-02 Un correo sin cuenta da el mismo error y gasta el mismo tiempo de verificación")
        void niegaElAccesoSinDelatarQueElCorreoNoExiste() {
            // Dado
            when(usuarioRepository.buscarPorCorreo(any())).thenReturn(Optional.empty());

            // Cuando / Entonces
            assertThatThrownBy(() -> iniciarSesion.ejecutar(new IniciarSesionComando("nadie@correo.com", FRASE)))
                    .isInstanceOf(CredencialesIncorrectasException.class)
                    .hasMessage("El correo o la frase de contraseña no son correctos.");
            verify(codificadorFrases).verificarContraHashFicticio(FRASE);
        }

        @Test
        @Tag("CP-IDE-003-02")
        @DisplayName("Un correo mal escrito se trata como credenciales incorrectas")
        void tratanElCorreoMalEscritoComoCredencialesIncorrectas() {
            assertThatThrownBy(() -> iniciarSesion.ejecutar(new IniciarSesionComando("no-es-correo", FRASE)))
                    .isInstanceOf(CredencialesIncorrectasException.class);
            verify(codificadorFrases).verificarContraHashFicticio(FRASE);
        }

        @Test
        @Tag("CP-IDE-003-03")
        @DisplayName("CP-IDE-003-03 Una tienda suspendida con credenciales correctas sabe que está suspendida")
        void informaQueLaCuentaEstaSuspendida() {
            // Dado
            var tiendaSuspendida = usuario(Rol.TIENDA, EstadoCuenta.SUSPENDIDA);
            when(usuarioRepository.buscarPorCorreo(any())).thenReturn(Optional.of(tiendaSuspendida));
            when(codificadorFrases.coincide(FRASE, HASH)).thenReturn(true);

            // Cuando / Entonces
            assertThatThrownBy(() -> iniciarSesion.ejecutar(new IniciarSesionComando("ana@correo.com", FRASE)))
                    .isInstanceOf(CuentaSuspendidaException.class)
                    .hasMessageContaining("suspendida")
                    .extracting("codigo")
                    .isEqualTo("IDE-009");
            verifyNoInteractions(tokenRefrescoRepository);
        }

        @Test
        @DisplayName("Un administrador sin secreto TOTP configurado no entra (IDE-011)")
        void exigeSegundoFactorAlAdministrador() {
            when(usuarioRepository.buscarPorCorreo(any()))
                    .thenReturn(Optional.of(usuario(Rol.ADMIN, EstadoCuenta.ACTIVA)));
            when(codificadorFrases.coincide(FRASE, HASH)).thenReturn(true);

            assertThatThrownBy(() -> iniciarSesion.ejecutar(new IniciarSesionComando("ana@correo.com", FRASE)))
                    .isInstanceOf(SegundoFactorRequeridoException.class);
            verifyNoInteractions(tokenRefrescoRepository);
        }
    }

    @Nested
    @DisplayName("Renovar la sesión")
    class RefrescarSesionTest {

        RefrescarSesion refrescarSesion;
        ValorTokenOpaco valorRecibido = ValorTokenOpaco.generar();

        @BeforeEach
        void configurar() {
            refrescarSesion = new RefrescarSesion(tokenRefrescoRepository, usuarioRepository, emisorSesion, reloj);
        }

        private TokenRefresco tokenGuardado(UUID usuarioId, boolean estaRevocado, Instant fechaExpiracion) {
            return TokenRefresco.reconstituir(
                    UUID.randomUUID(), usuarioId, valorRecibido.calcularHash(), fechaExpiracion, estaRevocado, AHORA);
        }

        @Test
        @DisplayName("Rota el token: revoca el usado y entrega uno nuevo")
        void rotaElToken() {
            // Dado
            var cliente = usuario(Rol.CLIENTE, EstadoCuenta.ACTIVA);
            var tokenActual = tokenGuardado(cliente.getId(), false, AHORA.plus(SIETE_DIAS));
            when(tokenRefrescoRepository.buscarPorHash(valorRecibido.calcularHash()))
                    .thenReturn(Optional.of(tokenActual));
            when(tokenRefrescoRepository.revocarSiSigueActivo(tokenActual.getId(), AHORA))
                    .thenReturn(true);
            when(usuarioRepository.buscarPorId(cliente.getId())).thenReturn(Optional.of(cliente));
            when(generadorTokenAcceso.generar(cliente.getId(), Rol.CLIENTE))
                    .thenReturn(new TokenAcceso("jwt-nuevo", AHORA.plus(Duration.ofMinutes(15))));

            // Cuando
            var sesion = refrescarSesion.ejecutar(valorRecibido.valor());

            // Entonces
            assertThat(sesion.tokenAcceso()).isEqualTo("jwt-nuevo");
            assertThat(sesion.tokenRefresco()).isNotEqualTo(valorRecibido.valor());
            verify(tokenRefrescoRepository).guardar(any());
        }

        @Test
        @DisplayName("Si llega un token ya revocado cierra todas las sesiones del usuario")
        void cierraTodasLasSesionesAnteElReusoDeUnToken() {
            // Dado: alguien copió un token que ya se había rotado
            var usuarioId = UUID.randomUUID();
            when(tokenRefrescoRepository.buscarPorHash(any()))
                    .thenReturn(Optional.of(tokenGuardado(usuarioId, true, AHORA.plus(SIETE_DIAS))));

            // Cuando / Entonces
            assertThatThrownBy(() -> refrescarSesion.ejecutar(valorRecibido.valor()))
                    .isInstanceOf(SesionVencidaException.class);
            verify(tokenRefrescoRepository).revocarTodosDe(usuarioId, AHORA);
            verify(tokenRefrescoRepository, never()).guardar(any());
        }

        @Test
        @DisplayName("Si otra petición rotó el mismo token primero, también se trata como reuso")
        void tratanLaRotacionSimultaneaComoReuso() {
            var usuarioId = UUID.randomUUID();
            var tokenActual = tokenGuardado(usuarioId, false, AHORA.plus(SIETE_DIAS));
            when(tokenRefrescoRepository.buscarPorHash(any())).thenReturn(Optional.of(tokenActual));
            when(tokenRefrescoRepository.revocarSiSigueActivo(tokenActual.getId(), AHORA))
                    .thenReturn(false);

            assertThatThrownBy(() -> refrescarSesion.ejecutar(valorRecibido.valor()))
                    .isInstanceOf(SesionVencidaException.class);
            verify(tokenRefrescoRepository).revocarTodosDe(usuarioId, AHORA);
        }

        @Test
        @DisplayName("Un token vencido no renueva la sesión")
        void rechazaUnTokenVencido() {
            var tokenVencido = tokenGuardado(UUID.randomUUID(), false, AHORA.minusSeconds(1));
            when(tokenRefrescoRepository.buscarPorHash(any())).thenReturn(Optional.of(tokenVencido));
            when(tokenRefrescoRepository.revocarSiSigueActivo(tokenVencido.getId(), AHORA))
                    .thenReturn(true);

            assertThatThrownBy(() -> refrescarSesion.ejecutar(valorRecibido.valor()))
                    .isInstanceOf(SesionVencidaException.class)
                    .extracting("codigo")
                    .isEqualTo("IDE-010");
            verify(tokenRefrescoRepository, never()).guardar(any());
        }

        @Test
        @Tag("CP-IDE-003-03")
        @DisplayName("Una cuenta suspendida no puede renovar la sesión")
        void rechazaLaRenovacionDeUnaCuentaSuspendida() {
            var tiendaSuspendida = usuario(Rol.TIENDA, EstadoCuenta.SUSPENDIDA);
            var tokenActual = tokenGuardado(tiendaSuspendida.getId(), false, AHORA.plus(SIETE_DIAS));
            when(tokenRefrescoRepository.buscarPorHash(any())).thenReturn(Optional.of(tokenActual));
            when(tokenRefrescoRepository.revocarSiSigueActivo(tokenActual.getId(), AHORA))
                    .thenReturn(true);
            when(usuarioRepository.buscarPorId(tiendaSuspendida.getId())).thenReturn(Optional.of(tiendaSuspendida));

            assertThatThrownBy(() -> refrescarSesion.ejecutar(valorRecibido.valor()))
                    .isInstanceOf(SesionVencidaException.class);
        }

        @Test
        @DisplayName("Sin cookie o con un token desconocido responde sesión vencida")
        void rechazaLaFaltaDeToken() {
            assertThatThrownBy(() -> refrescarSesion.ejecutar(null)).isInstanceOf(SesionVencidaException.class);
            assertThatThrownBy(() -> refrescarSesion.ejecutar(" ")).isInstanceOf(SesionVencidaException.class);
            when(tokenRefrescoRepository.buscarPorHash(any())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> refrescarSesion.ejecutar("desconocido"))
                    .isInstanceOf(SesionVencidaException.class);
        }
    }

    @Nested
    @DisplayName("Cerrar sesión")
    class CerrarSesionTest {

        CerrarSesion cerrarSesion;

        @BeforeEach
        void configurar() {
            cerrarSesion = new CerrarSesion(tokenRefrescoRepository, reloj);
        }

        @Test
        @DisplayName("Revoca el token de refresco de la cookie")
        void revocaElToken() {
            // Dado
            var valor = ValorTokenOpaco.generar();
            var token = TokenRefresco.emitir(UUID.randomUUID(), UUID.randomUUID(), valor, SIETE_DIAS, AHORA);
            when(tokenRefrescoRepository.buscarPorHash(valor.calcularHash())).thenReturn(Optional.of(token));

            // Cuando
            cerrarSesion.ejecutar(valor.valor());

            // Entonces
            verify(tokenRefrescoRepository).revocarSiSigueActivo(token.getId(), AHORA);
        }

        @Test
        @DisplayName("Cerrar una sesión sin cookie o ya cerrada no es un error")
        void esIdempotente() {
            cerrarSesion.ejecutar(null);
            when(tokenRefrescoRepository.buscarPorHash(any())).thenReturn(Optional.empty());
            cerrarSesion.ejecutar("ya-no-existe");

            verify(tokenRefrescoRepository, never()).revocarSiSigueActivo(any(), any());
        }
    }
}
