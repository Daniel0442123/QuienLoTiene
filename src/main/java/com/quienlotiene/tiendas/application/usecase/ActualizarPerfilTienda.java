package com.quienlotiene.tiendas.application.usecase;

import com.quienlotiene.tiendas.application.dto.ActualizarPerfilTiendaComando;
import com.quienlotiene.tiendas.application.dto.MiTienda;
import com.quienlotiene.tiendas.domain.model.HorarioAtencion;
import com.quienlotiene.tiendas.domain.model.HorarioInvalidoException;
import com.quienlotiene.tiendas.domain.model.TiendaNoEncontradaException;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.time.DayOfWeek;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-009 Configurar perfil y horario (CU-16 pasos 6 a 13). El horario se usa al buscar tiendas elegibles para una
 * solicitud (HU-017). Solo cambia la tienda del usuario que inició sesión (autorización por objeto, R-03).
 */
@Slf4j
@Service
public class ActualizarPerfilTienda {

    private final TiendaRepository tiendaRepository;

    ActualizarPerfilTienda(TiendaRepository tiendaRepository) {
        this.tiendaRepository = tiendaRepository;
    }

    @Transactional
    public MiTienda ejecutar(ActualizarPerfilTiendaComando comando) {
        var tienda = tiendaRepository.buscarPorDueno(comando.duenoId()).orElseThrow(TiendaNoEncontradaException::new);
        var horario = HorarioAtencion.de(comando.horario().stream()
                .map(franja ->
                        new HorarioAtencion.FranjaHoraria(dia(franja.diaSemana()), franja.abre(), franja.cierra()))
                .toList());
        tienda.actualizarPerfil(comando.nombre(), comando.telefono(), horario);
        tiendaRepository.guardarPerfil(tienda);
        log.info("Tienda {} actualizó su perfil", tienda.getId());
        return MiTiendaMapper.aMiTienda(tienda);
    }

    private static DayOfWeek dia(String diaSemana) {
        if (diaSemana == null) {
            throw new HorarioInvalidoException("falta el día de una franja");
        }
        try {
            return DayOfWeek.valueOf(diaSemana);
        } catch (IllegalArgumentException diaDesconocido) {
            throw new HorarioInvalidoException("el día debe ser MONDAY, TUESDAY, ..., SUNDAY");
        }
    }
}
