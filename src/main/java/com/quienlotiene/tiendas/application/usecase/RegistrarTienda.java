package com.quienlotiene.tiendas.application.usecase;

import com.quienlotiene.identidad.api.NuevoDuenoTienda;
import com.quienlotiene.identidad.api.RegistroUsuariosApi;
import com.quienlotiene.shared.domain.AlmacenArchivos;
import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.tiendas.application.dto.RegistrarTiendaComando;
import com.quienlotiene.tiendas.application.dto.TiendaRegistrada;
import com.quienlotiene.tiendas.domain.model.ArchivoSubido;
import com.quienlotiene.tiendas.domain.model.DocumentoVerificacion;
import com.quienlotiene.tiendas.domain.model.Tienda;
import com.quienlotiene.tiendas.domain.model.TipoDocumento;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * HU-002 Registrar tienda (CU-15): crea la cuenta del dueño (por la fachada de identidad), guarda los documentos y
 * deja la tienda PENDIENTE hasta que el administrador la apruebe. Si algo falla, la transacción se revierte y se
 * borran los archivos ya guardados.
 */
@Slf4j
@Service
public class RegistrarTienda {

    private final RegistroUsuariosApi registroUsuarios;
    private final TiendaRepository tiendaRepository;
    private final AlmacenArchivos almacenArchivos;
    private final Clock reloj;

    RegistrarTienda(
            RegistroUsuariosApi registroUsuarios,
            TiendaRepository tiendaRepository,
            AlmacenArchivos almacenArchivos,
            Clock reloj) {
        this.registroUsuarios = registroUsuarios;
        this.tiendaRepository = tiendaRepository;
        this.almacenArchivos = almacenArchivos;
        this.reloj = reloj;
    }

    @Transactional
    public TiendaRegistrada ejecutar(RegistrarTiendaComando comando) {
        // Primero lo barato: los documentos se validan antes de crear la cuenta y de calcular el hash de la frase
        var archivos = List.of(
                ArchivoSubido.validar(TipoDocumento.FOTO_LOCAL, comando.fotoLocal()),
                ArchivoSubido.validar(TipoDocumento.RUT, comando.rut()));
        var ahora = reloj.instant();

        var duenoId = registroUsuarios.registrarDuenoTienda(new NuevoDuenoTienda(
                comando.nombreDueno(),
                comando.correo(),
                comando.telefono(),
                comando.fraseContrasena(),
                comando.aceptaPoliticaDatos(),
                comando.versionPoliticaDatos()));
        var tienda = Tienda.registrar(
                UUID.randomUUID(),
                duenoId,
                comando.categoriaId(),
                comando.nombreTienda(),
                comando.telefono(),
                new Ubicacion(comando.latitud(), comando.longitud()),
                ahora);

        var documentos = guardarDocumentos(tienda.getId(), archivos, ahora);
        tiendaRepository.guardarNueva(tienda, documentos);
        log.info("Tienda {} registrada por el usuario {}, pendiente de revisión", tienda.getId(), duenoId);

        return new TiendaRegistrada(
                tienda.getId(), tienda.getNombre(), tienda.getEstado().name(), tienda.getFechaRegistro());
    }

    private List<DocumentoVerificacion> guardarDocumentos(UUID tiendaId, List<ArchivoSubido> archivos, Instant ahora) {
        var rutasGuardadas = new ArrayList<String>();
        borrarSiLaTransaccionNoTermina(rutasGuardadas);
        var documentos = new ArrayList<DocumentoVerificacion>();
        for (var archivo : archivos) {
            var ruta = almacenArchivos.guardar(
                    "tiendas/" + tiendaId, archivo.tipoArchivo().extension(), archivo.contenido());
            rutasGuardadas.add(ruta);
            documentos.add(new DocumentoVerificacion(
                    UUID.randomUUID(),
                    tiendaId,
                    archivo.tipoDocumento(),
                    ruta,
                    archivo.tipoArchivo().tipoContenido(),
                    archivo.tamanoEnBytes(),
                    ahora));
        }
        return documentos;
    }

    /** CU-15: si la transacción se revierte, los archivos guardados no deben quedar huérfanos en el disco. */
    private void borrarSiLaTransaccionNoTermina(List<String> rutasGuardadas) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int estado) {
                if (estado != STATUS_COMMITTED) {
                    rutasGuardadas.forEach(almacenArchivos::borrar);
                }
            }
        });
    }
}
