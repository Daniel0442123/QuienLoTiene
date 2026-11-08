package com.quienlotiene.identidad.application.usecase;

import com.quienlotiene.identidad.application.dto.RegistrarAdministradorComando;
import com.quienlotiene.identidad.domain.model.Correo;
import com.quienlotiene.identidad.domain.model.FraseContrasena;
import com.quienlotiene.identidad.domain.model.FraseContrasenaComunException;
import com.quienlotiene.identidad.domain.model.SecretoTotp;
import com.quienlotiene.identidad.domain.model.Usuario;
import com.quienlotiene.identidad.domain.port.CifradorSecretos;
import com.quienlotiene.identidad.domain.port.CodificadorFrasesContrasena;
import com.quienlotiene.identidad.domain.port.ListaFrasesComunes;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import java.time.Clock;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el administrador con su secreto TOTP cifrado (HU-004). No hay registro público de administradores: se crea al
 * desplegar con las variables {@code QLT_ADMIN_*}. Si el correo ya existe no cambia nada.
 */
@Service
public class RegistrarAdministrador {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("auditoria");

    private final UsuarioRepository usuarioRepository;
    private final CodificadorFrasesContrasena codificadorFrases;
    private final ListaFrasesComunes listaFrasesComunes;
    private final CifradorSecretos cifradorSecretos;
    private final Clock reloj;

    RegistrarAdministrador(
            UsuarioRepository usuarioRepository,
            CodificadorFrasesContrasena codificadorFrases,
            ListaFrasesComunes listaFrasesComunes,
            CifradorSecretos cifradorSecretos,
            Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorFrases = codificadorFrases;
        this.listaFrasesComunes = listaFrasesComunes;
        this.cifradorSecretos = cifradorSecretos;
        this.reloj = reloj;
    }

    /** @return {@code true} si lo creó; {@code false} si ya existía una cuenta con ese correo */
    @Transactional
    public boolean ejecutar(RegistrarAdministradorComando comando) {
        var correo = new Correo(comando.correo());
        if (usuarioRepository.existePorCorreo(correo)) {
            return false;
        }
        var frase = new FraseContrasena(comando.fraseContrasena());
        if (listaFrasesComunes.contiene(frase)) {
            throw new FraseContrasenaComunException();
        }
        var secreto = new SecretoTotp(comando.secretoTotpBase32());

        var administrador = Usuario.registrarAdministrador(
                UUID.randomUUID(),
                comando.nombre(),
                correo,
                comando.telefono(),
                codificadorFrases.calcularHash(frase),
                cifradorSecretos.cifrar(secreto.valorBase32()),
                reloj.instant());
        usuarioRepository.guardarNuevo(administrador);
        AUDITORIA.info("Administrador {} creado", administrador.getId());
        return true;
    }
}
