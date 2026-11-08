package com.quienlotiene.identidad.application.usecase;

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
import java.time.Instant;
import java.util.UUID;

/**
 * Pasos comunes de registrar un cliente (HU-001) o el dueño de una tienda (HU-002): autorización de datos vigente
 * (HU-005), correo único, frase de 15 o más caracteres que no sea común, hash Argon2id y guardado de la cuenta con su
 * autorización. No es un bean: cada caso de uso arma el suyo con sus mismas dependencias.
 */
final class RegistroConAutorizacion {

    /** Arma el usuario con el rol que corresponde a cada caso de uso. */
    @FunctionalInterface
    interface FabricaUsuario {
        Usuario crear(UUID id, String nombre, Correo correo, String telefono, String hashContrasena, Instant ahora);
    }

    record DatosRegistro(
            String nombre,
            String correo,
            String telefono,
            String fraseContrasena,
            boolean aceptaPoliticaDatos,
            String versionPoliticaDatos) {}

    private final UsuarioRepository usuarioRepository;
    private final CodificadorFrasesContrasena codificadorFrases;
    private final ListaFrasesComunes listaFrasesComunes;
    private final AutorizacionDatosRepository autorizacionDatosRepository;
    private final PoliticaDatos politicaDatos;
    private final Clock reloj;

    RegistroConAutorizacion(
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

    Usuario registrar(DatosRegistro datos, FabricaUsuario fabrica) {
        var ahora = reloj.instant();
        var usuarioId = UUID.randomUUID();
        var autorizacion = AutorizacionDatos.otorgar(
                UUID.randomUUID(),
                usuarioId,
                datos.aceptaPoliticaDatos(),
                datos.versionPoliticaDatos(),
                politicaDatos.versionVigente(),
                ahora);
        var correo = new Correo(datos.correo());
        var frase = new FraseContrasena(datos.fraseContrasena());
        if (listaFrasesComunes.contiene(frase)) {
            throw new FraseContrasenaComunException();
        }
        if (usuarioRepository.existePorCorreo(correo)) {
            throw new CorreoYaRegistradoException();
        }

        var usuario = fabrica.crear(
                usuarioId, datos.nombre(), correo, datos.telefono(), codificadorFrases.calcularHash(frase), ahora);
        usuarioRepository.guardarNuevo(usuario);
        autorizacionDatosRepository.guardar(autorizacion);
        return usuario;
    }
}
