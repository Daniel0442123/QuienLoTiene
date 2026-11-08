package com.quienlotiene.identidad.application.usecase;

import com.quienlotiene.identidad.application.dto.ClienteRegistrado;
import com.quienlotiene.identidad.application.dto.RegistrarClienteComando;
import com.quienlotiene.identidad.domain.model.AutorizacionDatos;
import com.quienlotiene.identidad.domain.model.Correo;
import com.quienlotiene.identidad.domain.model.CorreoYaRegistradoException;
import com.quienlotiene.identidad.domain.model.FraseContrasena;
import com.quienlotiene.identidad.domain.model.FraseContrasenaComunException;
import com.quienlotiene.identidad.domain.model.Usuario;
import com.quienlotiene.identidad.domain.port.AutorizacionDatosRepository;
import com.quienlotiene.identidad.domain.port.CodificadorFrasesContrasena;
import com.quienlotiene.identidad.domain.port.ListaFrasesComunes;
import com.quienlotiene.identidad.domain.port.PoliticaDatos;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import java.time.Clock;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-001 Registrar cliente (CU-01): crea una cuenta con rol {@code CLIENTE}, activa, con la frase de contraseña
 * guardada como hash Argon2id. Rechaza correos repetidos y frases cortas o comunes. Incluye HU-005: sin aceptar la
 * política de datos vigente no se crea la cuenta, y la aceptación se guarda con su versión y fecha.
 */
@Slf4j
@Service
public class RegistrarCliente {

    private final UsuarioRepository usuarioRepository;
    private final CodificadorFrasesContrasena codificadorFrases;
    private final ListaFrasesComunes listaFrasesComunes;
    private final AutorizacionDatosRepository autorizacionDatosRepository;
    private final PoliticaDatos politicaDatos;
    private final Clock reloj;

    public RegistrarCliente(
            UsuarioRepository usuarioRepository,
            CodificadorFrasesContrasena codificadorFrases,
            ListaFrasesComunes listaFrasesComunes,
            AutorizacionDatosRepository autorizacionDatosRepository,
            PoliticaDatos politicaDatos,
            Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorFrases = codificadorFrases;
        this.listaFrasesComunes = listaFrasesComunes;
        this.autorizacionDatosRepository = autorizacionDatosRepository;
        this.politicaDatos = politicaDatos;
        this.reloj = reloj;
    }

    @Transactional
    public ClienteRegistrado ejecutar(RegistrarClienteComando comando) {
        var ahora = reloj.instant();
        var usuarioId = UUID.randomUUID();
        var autorizacion = AutorizacionDatos.otorgar(
                UUID.randomUUID(),
                usuarioId,
                comando.aceptaPoliticaDatos(),
                comando.versionPoliticaDatos(),
                politicaDatos.versionVigente(),
                ahora);
        var correo = new Correo(comando.correo());
        var frase = new FraseContrasena(comando.fraseContrasena());
        if (listaFrasesComunes.contiene(frase)) {
            throw new FraseContrasenaComunException();
        }
        if (usuarioRepository.existePorCorreo(correo)) {
            throw new CorreoYaRegistradoException();
        }

        var cliente = Usuario.registrarCliente(
                usuarioId, comando.nombre(), correo, comando.telefono(), codificadorFrases.calcularHash(frase), ahora);
        usuarioRepository.guardarNuevo(cliente);
        autorizacionDatosRepository.guardar(autorizacion);
        log.info(
                "Cliente {} registrado con la política de datos {}",
                cliente.getId(),
                autorizacion.getVersionPolitica());

        return new ClienteRegistrado(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getCorreo().valor(),
                cliente.getRol().name(),
                cliente.getFechaCreacion());
    }
}
