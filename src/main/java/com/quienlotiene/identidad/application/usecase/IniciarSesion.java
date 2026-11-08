package com.quienlotiene.identidad.application.usecase;

import com.quienlotiene.identidad.application.dto.IniciarSesionComando;
import com.quienlotiene.identidad.application.dto.SesionIniciada;
import com.quienlotiene.identidad.domain.model.Correo;
import com.quienlotiene.identidad.domain.model.CorreoInvalidoException;
import com.quienlotiene.identidad.domain.model.CredencialesIncorrectasException;
import com.quienlotiene.identidad.domain.model.CuentaSuspendidaException;
import com.quienlotiene.identidad.domain.model.Rol;
import com.quienlotiene.identidad.domain.model.SegundoFactorRequeridoException;
import com.quienlotiene.identidad.domain.model.Usuario;
import com.quienlotiene.identidad.domain.port.CodificadorFrasesContrasena;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import java.time.Clock;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-003 Iniciar sesión (CU-02) para clientes y tiendas. Con credenciales correctas entrega un token de acceso de 15
 * minutos y un token de refresco. El error de credenciales no dice si falló el correo o la frase; la suspensión solo
 * se informa cuando las credenciales son correctas (CP-IDE-003-03).
 */
@Slf4j
@Service
public class IniciarSesion {

    private final UsuarioRepository usuarioRepository;
    private final CodificadorFrasesContrasena codificadorFrases;
    private final EmisorSesion emisorSesion;
    private final Clock reloj;

    IniciarSesion(
            UsuarioRepository usuarioRepository,
            CodificadorFrasesContrasena codificadorFrases,
            EmisorSesion emisorSesion,
            Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorFrases = codificadorFrases;
        this.emisorSesion = emisorSesion;
        this.reloj = reloj;
    }

    @Transactional
    public SesionIniciada ejecutar(IniciarSesionComando comando) {
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
        if (usuario.getRol() == Rol.ADMIN) {
            // TODO HU-004: el administrador entra con código TOTP (CU-25)
            throw new SegundoFactorRequeridoException();
        }

        var sesion = emisorSesion.emitirPara(usuario, reloj.instant());
        log.info("Usuario {} inició sesión", usuario.getId());
        return sesion;
    }

    private Optional<Usuario> buscarUsuario(String correoIngresado) {
        try {
            return usuarioRepository.buscarPorCorreo(new Correo(correoIngresado));
        } catch (CorreoInvalidoException correoMalEscrito) {
            return Optional.empty();
        }
    }
}
