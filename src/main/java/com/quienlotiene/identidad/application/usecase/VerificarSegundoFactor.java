package com.quienlotiene.identidad.application.usecase;

import com.quienlotiene.identidad.application.dto.SesionIniciada;
import com.quienlotiene.identidad.application.dto.VerificarSegundoFactorComando;
import com.quienlotiene.identidad.domain.model.CodigoSegundoFactorIncorrectoException;
import com.quienlotiene.identidad.domain.model.DesafioSegundoFactor;
import com.quienlotiene.identidad.domain.model.SecretoTotp;
import com.quienlotiene.identidad.domain.model.ValorTokenOpaco;
import com.quienlotiene.identidad.domain.model.VerificacionVencidaException;
import com.quienlotiene.identidad.domain.port.CifradorSecretos;
import com.quienlotiene.identidad.domain.port.DesafioSegundoFactorRepository;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import com.quienlotiene.identidad.domain.port.VerificadorCodigoTotp;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-004 Segundo factor del administrador (CU-25, pasos 9 a 16): con el token temporal y un código TOTP vigente
 * entrega la sesión con rol ADMIN. Cada intento, bueno o malo, queda en el log de auditoría (RNF-013).
 */
@Service
public class VerificarSegundoFactor {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("auditoria");

    private final DesafioSegundoFactorRepository desafioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CifradorSecretos cifradorSecretos;
    private final VerificadorCodigoTotp verificadorTotp;
    private final EmisorSesion emisorSesion;
    private final Clock reloj;

    VerificarSegundoFactor(
            DesafioSegundoFactorRepository desafioRepository,
            UsuarioRepository usuarioRepository,
            CifradorSecretos cifradorSecretos,
            VerificadorCodigoTotp verificadorTotp,
            EmisorSesion emisorSesion,
            Clock reloj) {
        this.desafioRepository = desafioRepository;
        this.usuarioRepository = usuarioRepository;
        this.cifradorSecretos = cifradorSecretos;
        this.verificadorTotp = verificadorTotp;
        this.emisorSesion = emisorSesion;
        this.reloj = reloj;
    }

    /** El intento fallido debe quedar guardado aunque la respuesta sea un error: por eso no se revierte. */
    @Transactional(noRollbackFor = CodigoSegundoFactorIncorrectoException.class)
    public SesionIniciada ejecutar(VerificarSegundoFactorComando comando) {
        if (comando.tokenSegundoFactor() == null || comando.tokenSegundoFactor().isBlank()) {
            throw new VerificacionVencidaException();
        }
        var ahora = reloj.instant();
        var desafio = desafioRepository
                .buscarPorHash(new ValorTokenOpaco(comando.tokenSegundoFactor()).calcularHash())
                .filter(desafioEncontrado -> desafioEncontrado.admiteIntentos(ahora))
                .orElseThrow(VerificacionVencidaException::new);
        var administrador = usuarioRepository
                .buscarPorId(desafio.getUsuarioId())
                .filter(usuario -> usuario.estaActiva() && usuario.tieneSegundoFactor())
                .orElseThrow(VerificacionVencidaException::new);

        var secretoCifrado = administrador.getSecretoTotpCifrado().orElseThrow(VerificacionVencidaException::new);
        var secreto = new SecretoTotp(cifradorSecretos.descifrar(secretoCifrado));
        if (!verificadorTotp.esValido(secreto, comando.codigo())) {
            desafioRepository.registrarIntentoFallido(desafio.getId(), ahora);
            AUDITORIA.warn(
                    "Código TOTP incorrecto del administrador {} (intento {} de {})",
                    administrador.getId(),
                    desafio.getIntentosFallidos() + 1,
                    DesafioSegundoFactor.MAXIMO_INTENTOS_FALLIDOS);
            throw new CodigoSegundoFactorIncorrectoException();
        }
        if (!desafioRepository.marcarUsadoSiNoLoEsta(desafio.getId(), ahora)) {
            throw new VerificacionVencidaException();
        }

        var sesion = emisorSesion.emitirPara(administrador, ahora);
        AUDITORIA.info("El administrador {} inició sesión con segundo factor", administrador.getId());
        return sesion;
    }
}
