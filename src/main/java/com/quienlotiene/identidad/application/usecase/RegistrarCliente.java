package com.quienlotiene.identidad.application.usecase;

import com.quienlotiene.identidad.application.dto.ClienteRegistrado;
import com.quienlotiene.identidad.application.dto.RegistrarClienteComando;
import com.quienlotiene.identidad.domain.model.Usuario;
import com.quienlotiene.identidad.domain.port.AutorizacionDatosRepository;
import com.quienlotiene.identidad.domain.port.CodificadorFrasesContrasena;
import com.quienlotiene.identidad.domain.port.ListaFrasesComunes;
import com.quienlotiene.identidad.domain.port.PoliticaDatos;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import java.time.Clock;
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

    private final RegistroConAutorizacion registro;

    public RegistrarCliente(
            UsuarioRepository usuarioRepository,
            CodificadorFrasesContrasena codificadorFrases,
            ListaFrasesComunes listaFrasesComunes,
            AutorizacionDatosRepository autorizacionDatosRepository,
            PoliticaDatos politicaDatos,
            Clock reloj) {
        this.registro = new RegistroConAutorizacion(
                usuarioRepository,
                codificadorFrases,
                listaFrasesComunes,
                autorizacionDatosRepository,
                politicaDatos,
                reloj);
    }

    @Transactional
    public ClienteRegistrado ejecutar(RegistrarClienteComando comando) {
        var cliente = registro.registrar(
                new RegistroConAutorizacion.DatosRegistro(
                        comando.nombre(),
                        comando.correo(),
                        comando.telefono(),
                        comando.fraseContrasena(),
                        comando.aceptaPoliticaDatos(),
                        comando.versionPoliticaDatos()),
                Usuario::registrarCliente);
        log.info("Cliente {} registrado", cliente.getId());

        return new ClienteRegistrado(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getCorreo().valor(),
                cliente.getRol().name(),
                cliente.getFechaCreacion());
    }
}
