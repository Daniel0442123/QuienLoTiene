package com.quienlotiene.identidad.application.usecase;

import com.quienlotiene.identidad.application.dto.ClienteRegistrado;
import com.quienlotiene.identidad.application.dto.RegistrarClienteComando;
import com.quienlotiene.identidad.domain.model.Correo;
import com.quienlotiene.identidad.domain.model.CorreoYaRegistradoException;
import com.quienlotiene.identidad.domain.model.FraseContrasena;
import com.quienlotiene.identidad.domain.model.FraseContrasenaComunException;
import com.quienlotiene.identidad.domain.model.Usuario;
import com.quienlotiene.identidad.domain.port.CodificadorFrasesContrasena;
import com.quienlotiene.identidad.domain.port.ListaFrasesComunes;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import java.time.Clock;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-001 Registrar cliente (CU-01): crea una cuenta con rol {@code CLIENTE}, activa, con la frase de contraseña
 * guardada como hash Argon2id. Rechaza correos repetidos y frases cortas o comunes.
 */
@Slf4j
@Service
public class RegistrarCliente {

    private final UsuarioRepository usuarioRepository;
    private final CodificadorFrasesContrasena codificadorFrases;
    private final ListaFrasesComunes listaFrasesComunes;
    private final Clock reloj;

    public RegistrarCliente(
            UsuarioRepository usuarioRepository,
            CodificadorFrasesContrasena codificadorFrases,
            ListaFrasesComunes listaFrasesComunes,
            Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorFrases = codificadorFrases;
        this.listaFrasesComunes = listaFrasesComunes;
        this.reloj = reloj;
    }

    @Transactional
    public ClienteRegistrado ejecutar(RegistrarClienteComando comando) {
        var correo = new Correo(comando.correo());
        var frase = new FraseContrasena(comando.fraseContrasena());
        if (listaFrasesComunes.contiene(frase)) {
            throw new FraseContrasenaComunException();
        }
        if (usuarioRepository.existePorCorreo(correo)) {
            throw new CorreoYaRegistradoException();
        }

        var cliente = Usuario.registrarCliente(
                UUID.randomUUID(),
                comando.nombre(),
                correo,
                comando.telefono(),
                codificadorFrases.calcularHash(frase),
                reloj.instant());
        usuarioRepository.guardarNuevo(cliente);
        log.info("Cliente {} registrado", cliente.getId());

        return new ClienteRegistrado(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getCorreo().valor(),
                cliente.getRol().name(),
                cliente.getFechaCreacion());
    }
}
