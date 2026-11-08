package com.quienlotiene.identidad.application.usecase;

import com.quienlotiene.identidad.api.NuevoDuenoTienda;
import com.quienlotiene.identidad.api.RegistroUsuariosApi;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-002 (CU-15 pasos 5 a 7): crea la cuenta del dueño de una tienda con rol {@code TIENDA}, con las mismas reglas
 * del registro de clientes. La llama el módulo tiendas por {@link RegistroUsuariosApi}.
 */
@Slf4j
@Service
class RegistrarDuenoTienda implements RegistroUsuariosApi {

    private final RegistroConAutorizacion registro;

    RegistrarDuenoTienda(
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

    /** Exige una transacción abierta: la cuenta y la tienda se guardan juntas o no se guarda ninguna. */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID registrarDuenoTienda(NuevoDuenoTienda dueno) {
        var duenoRegistrado = registro.registrar(
                new RegistroConAutorizacion.DatosRegistro(
                        dueno.nombre(),
                        dueno.correo(),
                        dueno.telefono(),
                        dueno.fraseContrasena(),
                        dueno.aceptaPoliticaDatos(),
                        dueno.versionPoliticaDatos()),
                Usuario::registrarDuenoTienda);
        log.info("Dueño de tienda {} registrado", duenoRegistrado.getId());
        return duenoRegistrado.getId();
    }
}
