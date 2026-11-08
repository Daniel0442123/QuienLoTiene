package com.quienlotiene.identidad.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.quienlotiene.identidad.application.dto.IniciarSesionComando;
import com.quienlotiene.identidad.application.dto.RegistrarAdministradorComando;
import com.quienlotiene.identidad.application.dto.SegundoFactorPendiente;
import com.quienlotiene.identidad.application.dto.VerificarSegundoFactorComando;
import com.quienlotiene.identidad.domain.model.CodigoSegundoFactorIncorrectoException;
import com.quienlotiene.identidad.domain.model.Correo;
import com.quienlotiene.identidad.domain.model.DesafioSegundoFactor;
import com.quienlotiene.identidad.domain.model.EstadoCuenta;
import com.quienlotiene.identidad.domain.model.FraseContrasenaComunException;
import com.quienlotiene.identidad.domain.model.Rol;
import com.quienlotiene.identidad.domain.model.SecretoTotp;
import com.quienlotiene.identidad.domain.model.SecretoTotpInvalidoException;
import com.quienlotiene.identidad.domain.model.TokenAcceso;
import com.quienlotiene.identidad.domain.model.Usuario;
import com.quienlotiene.identidad.domain.model.ValorTokenOpaco;
import com.quienlotiene.identidad.domain.model.VerificacionVencidaException;
import com.quienlotiene.identidad.domain.port.CifradorSecretos;
import com.quienlotiene.identidad.domain.port.CodificadorFrasesContrasena;
import com.quienlotiene.identidad.domain.port.DesafioSegundoFactorRepository;
import com.quienlotiene.identidad.domain.port.GeneradorTokenAcceso;
import com.quienlotiene.identidad.domain.port.ListaFrasesComunes;
import com.quienlotiene.identidad.domain.port.TokenRefrescoRepository;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import com.quienlotiene.identidad.domain.port.VerificadorCodigoTotp;
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
@Tag("HU-004")
@DisplayName("HU-004 Segundo factor del administrador")
class SegundoFactorTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:30:00Z");
    private static final String FRASE = "la clave del jefe es larga y segura";
    private static final String HASH = "$argon2id$hash";
    private static final String SECRETO = "JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP";

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    CodificadorFrasesContrasena codificadorFrases;

    @Mock
    DesafioSegundoFactorRepository desafioRepository;

    @Mock
    CifradorSecretos cifradorSecretos;

    @Mock
    VerificadorCodigoTotp verificadorTotp;

    @Mock
    GeneradorTokenAcceso generadorTokenAcceso;

    @Mock
    TokenRefrescoRepository tokenRefrescoRepository;

    Clock reloj = Clock.fixed(AHORA, ZoneOffset.UTC);
    EmisorSesion emisorSesion;

    @BeforeEach
    void configurar() {
        emisorSesion = new EmisorSesion(generadorTokenAcceso, tokenRefrescoRepository, () -> Duration.ofDays(7));
    }

    private static Usuario administrador(EstadoCuenta estado) {
        return Usuario.reconstituir(
                UUID.randomUUID(),
                "Admin",
                new Correo("admin@quienlotiene.app"),
                "3000000000",
                HASH,
                Rol.ADMIN,
                estado,
                "secreto-cifrado",
                AHORA);
    }

    @Test
    @DisplayName("Con la frase correcta el administrador recibe un token temporal de 5 minutos, no la sesión")
    void entregaUnTokenTemporalAlAdministrador() {
        // Dado
        var admin = administrador(EstadoCuenta.ACTIVA);
        when(usuarioRepository.buscarPorCorreo(any())).thenReturn(Optional.of(admin));
        when(codificadorFrases.coincide(FRASE, HASH)).thenReturn(true);
        var iniciarSesion =
                new IniciarSesion(usuarioRepository, codificadorFrases, emisorSesion, desafioRepository, reloj);

        // Cuando
        var resultado = iniciarSesion.ejecutar(new IniciarSesionComando("admin@quienlotiene.app", FRASE));

        // Entonces
        assertThat(resultado).isInstanceOf(SegundoFactorPendiente.class);
        var pendiente = (SegundoFactorPendiente) resultado;
        assertThat(pendiente.fechaExpiracion()).isEqualTo(AHORA.plus(Duration.ofMinutes(5)));
        var desafioGuardado = ArgumentCaptor.forClass(DesafioSegundoFactor.class);
        verify(desafioRepository).guardar(desafioGuardado.capture());
        assertThat(desafioGuardado.getValue().getUsuarioId()).isEqualTo(admin.getId());
        assertThat(desafioGuardado.getValue().getHashToken())
                .isEqualTo(new ValorTokenOpaco(pendiente.tokenSegundoFactor()).calcularHash());
        verifyNoInteractions(generadorTokenAcceso, tokenRefrescoRepository);
    }

    @Nested
    @DisplayName("Verificar el código")
    class VerificarCodigoTest {

        VerificarSegundoFactor verificarSegundoFactor;
        ValorTokenOpaco tokenTemporal = ValorTokenOpaco.generar();
        Usuario admin = administrador(EstadoCuenta.ACTIVA);

        @BeforeEach
        void configurar() {
            verificarSegundoFactor = new VerificarSegundoFactor(
                    desafioRepository, usuarioRepository, cifradorSecretos, verificadorTotp, emisorSesion, reloj);
        }

        private DesafioSegundoFactor desafio(int intentosFallidos, boolean fueUsado, Instant fechaExpiracion) {
            return DesafioSegundoFactor.reconstituir(
                    UUID.randomUUID(),
                    admin.getId(),
                    tokenTemporal.calcularHash(),
                    fechaExpiracion,
                    intentosFallidos,
                    fueUsado);
        }

        private void dadoUnDesafioVigente(DesafioSegundoFactor desafio) {
            when(desafioRepository.buscarPorHash(tokenTemporal.calcularHash())).thenReturn(Optional.of(desafio));
            when(usuarioRepository.buscarPorId(admin.getId())).thenReturn(Optional.of(admin));
            when(cifradorSecretos.descifrar("secreto-cifrado")).thenReturn(SECRETO);
        }

        @Test
        @Tag("CP-IDE-004-01")
        @DisplayName("CP-IDE-004-01 Con un código TOTP vigente entra al panel con rol ADMIN")
        void entregaLaSesionConUnCodigoVigente() {
            // Dado
            var desafio = desafio(0, false, AHORA.plus(Duration.ofMinutes(4)));
            dadoUnDesafioVigente(desafio);
            when(verificadorTotp.esValido(new SecretoTotp(SECRETO), "123456")).thenReturn(true);
            when(desafioRepository.marcarUsadoSiNoLoEsta(desafio.getId(), AHORA))
                    .thenReturn(true);
            when(generadorTokenAcceso.generar(admin.getId(), Rol.ADMIN))
                    .thenReturn(new TokenAcceso("jwt-admin", AHORA.plus(Duration.ofMinutes(15))));

            // Cuando
            var sesion =
                    verificarSegundoFactor.ejecutar(new VerificarSegundoFactorComando(tokenTemporal.valor(), "123456"));

            // Entonces
            assertThat(sesion.rol()).isEqualTo("ADMIN");
            assertThat(sesion.tokenAcceso()).isEqualTo("jwt-admin");
            verify(tokenRefrescoRepository).guardar(any());
        }

        @Test
        @Tag("CP-IDE-004-02")
        @DisplayName("CP-IDE-004-02 Con un código incorrecto niega el acceso y cuenta el intento")
        void niegaElAccesoConUnCodigoIncorrecto() {
            // Dado
            var desafio = desafio(2, false, AHORA.plus(Duration.ofMinutes(4)));
            dadoUnDesafioVigente(desafio);
            when(verificadorTotp.esValido(new SecretoTotp(SECRETO), "000000")).thenReturn(false);

            // Cuando / Entonces
            assertThatThrownBy(() -> verificarSegundoFactor.ejecutar(
                            new VerificarSegundoFactorComando(tokenTemporal.valor(), "000000")))
                    .isInstanceOf(CodigoSegundoFactorIncorrectoException.class)
                    .extracting("codigo")
                    .isEqualTo("IDE-012");
            verify(desafioRepository).registrarIntentoFallido(desafio.getId(), AHORA);
            verifyNoInteractions(generadorTokenAcceso, tokenRefrescoRepository);
        }

        @Test
        @Tag("CP-IDE-004-02")
        @DisplayName("Un token temporal vencido, usado o con 5 intentos fallidos ya no sirve")
        void rechazaUnDesafioQueYaNoAdmiteIntentos() {
            for (var desafioAgotado : new DesafioSegundoFactor[] {
                desafio(0, false, AHORA),
                desafio(0, true, AHORA.plusSeconds(60)),
                desafio(5, false, AHORA.plusSeconds(60))
            }) {
                when(desafioRepository.buscarPorHash(tokenTemporal.calcularHash()))
                        .thenReturn(Optional.of(desafioAgotado));

                assertThatThrownBy(() -> verificarSegundoFactor.ejecutar(
                                new VerificarSegundoFactorComando(tokenTemporal.valor(), "123456")))
                        .isInstanceOf(VerificacionVencidaException.class)
                        .extracting("codigo")
                        .isEqualTo("IDE-013");
            }
            verifyNoInteractions(verificadorTotp);
        }

        @Test
        @DisplayName("Si otra petición usó el mismo token primero, no entrega una segunda sesión")
        void noEntregaDosSesionesConElMismoToken() {
            var desafio = desafio(0, false, AHORA.plus(Duration.ofMinutes(4)));
            dadoUnDesafioVigente(desafio);
            when(verificadorTotp.esValido(any(), any())).thenReturn(true);
            when(desafioRepository.marcarUsadoSiNoLoEsta(desafio.getId(), AHORA))
                    .thenReturn(false);

            assertThatThrownBy(() -> verificarSegundoFactor.ejecutar(
                            new VerificarSegundoFactorComando(tokenTemporal.valor(), "123456")))
                    .isInstanceOf(VerificacionVencidaException.class);
            verify(tokenRefrescoRepository, never()).guardar(any());
        }

        @Test
        @DisplayName("Sin token o con un administrador suspendido no hay sesión")
        void rechazaSinTokenOConAdministradorSuspendido() {
            assertThatThrownBy(() -> verificarSegundoFactor.ejecutar(new VerificarSegundoFactorComando(" ", "123456")))
                    .isInstanceOf(VerificacionVencidaException.class);

            var suspendido = administrador(EstadoCuenta.SUSPENDIDA);
            when(desafioRepository.buscarPorHash(any()))
                    .thenReturn(Optional.of(DesafioSegundoFactor.reconstituir(
                            UUID.randomUUID(), suspendido.getId(), "hash", AHORA.plusSeconds(60), 0, false)));
            when(usuarioRepository.buscarPorId(suspendido.getId())).thenReturn(Optional.of(suspendido));
            assertThatThrownBy(() -> verificarSegundoFactor.ejecutar(
                            new VerificarSegundoFactorComando(tokenTemporal.valor(), "123456")))
                    .isInstanceOf(VerificacionVencidaException.class);
        }
    }

    @Nested
    @DisplayName("Crear el administrador inicial")
    class RegistrarAdministradorTest {

        @Mock
        ListaFrasesComunes listaFrasesComunes;

        RegistrarAdministrador registrarAdministrador;

        @BeforeEach
        void configurar() {
            registrarAdministrador = new RegistrarAdministrador(
                    usuarioRepository, codificadorFrases, listaFrasesComunes, cifradorSecretos, reloj);
        }

        private static RegistrarAdministradorComando comando(String frase, String secreto) {
            return new RegistrarAdministradorComando("Admin", "admin@quienlotiene.app", "3000000000", frase, secreto);
        }

        @Test
        @DisplayName("Crea el administrador activo con el secreto cifrado")
        void creaElAdministradorConElSecretoCifrado() {
            when(codificadorFrases.calcularHash(any())).thenReturn(HASH);
            when(cifradorSecretos.cifrar(SECRETO)).thenReturn("secreto-cifrado");

            var fueCreado = registrarAdministrador.ejecutar(comando(FRASE, "jbsw y3dp ehpk 3pxp jbsw y3dp ehpk 3pxp"));

            assertThat(fueCreado).isTrue();
            var guardado = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).guardarNuevo(guardado.capture());
            assertThat(guardado.getValue().getRol()).isEqualTo(Rol.ADMIN);
            assertThat(guardado.getValue().tieneSegundoFactor()).isTrue();
            assertThat(guardado.getValue().getSecretoTotpCifrado()).contains("secreto-cifrado");
        }

        @Test
        @DisplayName("Si el correo ya existe no cambia nada")
        void noCambiaUnAdministradorExistente() {
            when(usuarioRepository.existePorCorreo(new Correo("admin@quienlotiene.app")))
                    .thenReturn(true);

            assertThat(registrarAdministrador.ejecutar(comando(FRASE, SECRETO))).isFalse();
            verify(usuarioRepository, never()).guardarNuevo(any());
        }

        @Test
        @DisplayName("Rechaza un secreto corto o que no es Base32, y una frase común")
        void validaElSecretoYLaFrase() {
            assertThatThrownBy(() -> registrarAdministrador.ejecutar(comando(FRASE, "CORTO")))
                    .isInstanceOf(SecretoTotpInvalidoException.class)
                    .extracting("codigo")
                    .isEqualTo("IDE-014");
            assertThatThrownBy(() -> registrarAdministrador.ejecutar(comando(FRASE, "1".repeat(32))))
                    .isInstanceOf(SecretoTotpInvalidoException.class);
            assertThatThrownBy(() -> registrarAdministrador.ejecutar(comando(FRASE, null)))
                    .isInstanceOf(SecretoTotpInvalidoException.class);
            when(listaFrasesComunes.contiene(any())).thenReturn(true);
            assertThatThrownBy(() -> registrarAdministrador.ejecutar(comando(FRASE, SECRETO)))
                    .isInstanceOf(FraseContrasenaComunException.class);
            assertThat(new SecretoTotp(SECRETO).toString()).doesNotContain(SECRETO);
        }
    }
}
