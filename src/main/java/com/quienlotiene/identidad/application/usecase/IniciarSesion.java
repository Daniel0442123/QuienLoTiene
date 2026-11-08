package com.quienlotiene.identidad.application.usecase;

import com.quienlotiene.identidad.application.dto.IniciarSesionComando;
import com.quienlotiene.identidad.application.dto.ResultadoInicioSesion;
import com.quienlotiene.identidad.application.dto.SegundoFactorPendiente;
import com.quienlotiene.identidad.domain.model.Correo;
import com.quienlotiene.identidad.domain.model.CorreoInvalidoException;
import com.quienlotiene.identidad.domain.model.CredencialesIncorrectasException;
import com.quienlotiene.identidad.domain.model.CuentaSuspendidaException;
import com.quienlotiene.identidad.domain.model.DesafioSegundoFactor;
import com.quienlotiene.identidad.domain.model.SegundoFactorRequeridoException;
import com.quienlotiene.identidad.domain.model.Usuario;
import com.quienlotiene.identidad.domain.model.ValorTokenOpaco;
import com.quienlotiene.identidad.domain.port.CodificadorFrasesContrasena;
import com.quienlotiene.identidad.domain.port.DesafioSegundoFactorRepository;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-003 Iniciar sesión (CU-02) para clientes y tiendas. Con credenciales correctas entrega un token de acceso de 15
 * minutos y un token de refresco; el administrador recibe primero un token temporal para el código TOTP (HU-004,
 * CU-25). El error de credenciales no dice si falló el correo o la frase; la suspensión solo
 * se informa cuando las credenciales son correctas (CP-IDE-003-03).
 */
@Slf4j
@Service
public class IniciarSesion {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("auditoria");

    private final UsuarioRepository usuarioRepository;
    private final CodificadorFrasesContrasena codificadorFrases;
    private final EmisorSesion emisorSesion;
    private final DesafioSegundoFactorRepository desafioRepository;
    private final Clock reloj;

    IniciarSesion(
            UsuarioRepository usuarioRepository,
            CodificadorFrasesContrasena codificadorFrases,
            EmisorSesion emisorSesion,
            DesafioSegundoFactorRepository desafioRepository,
            Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorFrases = codificadorFrases;
        this.emisorSesion = emisorSesion;
        this.desafioRepository = desafioRepository;
        this.reloj = reloj;
    }

    @Transactional
    public ResultadoInicioSesion ejecutar(IniciarSesionComando comando) {
        var usuarioEncontrado = buscarUsuario(comando.correo());
        if (usuarioEncontrado.isEmpty()) {
            codificadorFrases.verificarContraHashFicticio(comando.fraseContrasena());
            throw new CredencialesIncorrectasException();
        }
        var usuario = usuarioEncontrado.get();
        if (!codificadorFrases.coincide(comando.fraseContrasena(), usuario.getHashContrasena())) {
            log.info("Inicio de sesión fallido para el usuario {}", usuario.getId());
            throw new CredencialesIncorrectasException();
        }
        if (!usuario.estaActiva()) {
            log.info("Inicio de sesión rechazado: el usuario {} está suspendido", usuario.getId());
            throw new CuentaSuspendidaException();
        }
        var ahora = reloj.instant();
        if (usuario.esAdministrador()) {
            return iniciarSegundoFactor(usuario, ahora);
        }

        var sesion = emisorSesion.emitirPara(usuario, ahora);
        log.info("Usuario {} inició sesión", usuario.getId());
        return sesion;
    }

    /** HU-004 (CU-25 pasos 5 y 6): la frase fue correcta; el administrador aún debe dar su código TOTP. */
    private SegundoFactorPendiente iniciarSegundoFactor(Usuario administrador, Instant ahora) {
        if (!administrador.tieneSegundoFactor()) {
            AUDITORIA.warn("El administrador {} no tiene segundo factor configurado", administrador.getId());
            throw new SegundoFactorRequeridoException();
        }
        var valorToken = ValorTokenOpaco.generar();
        var desafio = DesafioSegundoFactor.emitir(UUID.randomUUID(), administrador.getId(), valorToken, ahora);
        desafioRepository.guardar(desafio);
        AUDITORIA.info("El administrador {} pasó la frase y falta el segundo factor", administrador.getId());
        return new SegundoFactorPendiente(valorToken.valor(), desafio.getFechaExpiracion());
    }

    private Optional<Usuario> buscarUsuario(String correoIngresado) {
        try {
            return usuarioRepository.buscarPorCorreo(new Correo(correoIngresado));
        } catch (CorreoInvalidoException correoMalEscrito) {
            return Optional.empty();
        }
    }
}
