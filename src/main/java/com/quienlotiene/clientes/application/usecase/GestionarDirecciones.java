package com.quienlotiene.clientes.application.usecase;

import com.quienlotiene.clientes.application.dto.DatosDireccion;
import com.quienlotiene.clientes.domain.model.Direccion;
import com.quienlotiene.clientes.domain.model.DireccionIncompletaException;
import com.quienlotiene.clientes.domain.model.DireccionNoEncontradaException;
import com.quienlotiene.clientes.domain.model.LimiteDireccionesException;
import com.quienlotiene.clientes.domain.port.DireccionRepository;
import com.quienlotiene.shared.domain.Ubicacion;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-007 Gestionar direcciones del cliente (CU-03): listar, crear, editar y borrar. Cada operación revisa que la
 * dirección sea del cliente que inició sesión; si no, responde como si no existiera (R-03).
 */
@Slf4j
@Service
public class GestionarDirecciones {

    private final DireccionRepository direccionRepository;
    private final Clock reloj;

    GestionarDirecciones(DireccionRepository direccionRepository, Clock reloj) {
        this.direccionRepository = direccionRepository;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<DatosDireccion> listar(UUID clienteId) {
        return direccionRepository.listarDe(clienteId).stream()
                .map(GestionarDirecciones::aDatos)
                .toList();
    }

    @Transactional
    public DatosDireccion crear(UUID clienteId, DatosDireccion datos) {
        if (direccionRepository.contarDe(clienteId) >= Direccion.MAXIMO_POR_CLIENTE) {
            throw new LimiteDireccionesException();
        }
        var direccion = Direccion.registrar(
                UUID.randomUUID(),
                clienteId,
                datos.nombre(),
                ubicacionDe(datos),
                datos.direccionEscrita(),
                datos.indicaciones(),
                datos.telefono(),
                reloj.instant());
        direccionRepository.guardarNueva(direccion);
        log.info("Cliente {} guardó la dirección {}", clienteId, direccion.getId());
        return aDatos(direccion);
    }

    @Transactional
    public DatosDireccion editar(UUID clienteId, UUID direccionId, DatosDireccion datos) {
        var direccion = direccionDelCliente(clienteId, direccionId);
        direccion.cambiar(
                datos.nombre(), ubicacionDe(datos), datos.direccionEscrita(), datos.indicaciones(), datos.telefono());
        direccionRepository.guardarCambios(direccion);
        return aDatos(direccion);
    }

    @Transactional
    public void borrar(UUID clienteId, UUID direccionId) {
        direccionDelCliente(clienteId, direccionId);
        direccionRepository.borrar(direccionId);
        log.info("Cliente {} borró una dirección", clienteId);
    }

    private Direccion direccionDelCliente(UUID clienteId, UUID direccionId) {
        return direccionRepository
                .buscarPorId(direccionId)
                .filter(direccion -> direccion.esDe(clienteId))
                .orElseThrow(DireccionNoEncontradaException::new);
    }

    /** CP-CLI-007-02: sin el punto en el mapa la dirección no sirve para el domicilio. */
    private static Ubicacion ubicacionDe(DatosDireccion datos) {
        if (datos.latitud() == null || datos.longitud() == null) {
            throw new DireccionIncompletaException("marca la ubicación en el mapa");
        }
        return new Ubicacion(datos.latitud(), datos.longitud());
    }

    private static DatosDireccion aDatos(Direccion direccion) {
        return new DatosDireccion(
                direccion.getId(),
                direccion.getNombre(),
                direccion.getUbicacion().latitud(),
                direccion.getUbicacion().longitud(),
                direccion.getDireccionEscrita(),
                direccion.getIndicaciones(),
                direccion.getTelefono(),
                direccion.getFechaCreacion());
    }
}
