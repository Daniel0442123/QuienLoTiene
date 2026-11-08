package com.quienlotiene.tiendas.application.usecase;

import com.quienlotiene.tiendas.domain.model.MedioPago;
import com.quienlotiene.tiendas.domain.model.MediosPagoAceptados;
import com.quienlotiene.tiendas.domain.model.MediosPagoInvalidosException;
import com.quienlotiene.tiendas.domain.model.TiendaNoEncontradaException;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-012 Configurar medios de pago (CU-18): solo se registran; la app no cobra. */
@Slf4j
@Service
public class ConfigurarMediosPago {

    private final TiendaRepository tiendaRepository;

    ConfigurarMediosPago(TiendaRepository tiendaRepository) {
        this.tiendaRepository = tiendaRepository;
    }

    @Transactional(readOnly = true)
    public List<String> consultar(UUID duenoId) {
        return nombres(tiendaRepository.buscarMediosPago(tiendaDe(duenoId)));
    }

    /** CP-TIE-012-02: el medio que no se envía queda desactivado y deja de aparecer en las órdenes nuevas. */
    @Transactional
    public List<String> guardar(UUID duenoId, List<String> mediosPago) {
        var tiendaId = tiendaDe(duenoId);
        var aceptados = MediosPagoAceptados.de(
                mediosPago == null
                        ? List.of()
                        : mediosPago.stream().map(ConfigurarMediosPago::medio).toList());
        tiendaRepository.guardarMediosPago(tiendaId, aceptados);
        log.info("Tienda {} actualizó sus medios de pago", tiendaId);
        return nombres(aceptados);
    }

    private UUID tiendaDe(UUID duenoId) {
        return tiendaRepository
                .buscarPorDueno(duenoId)
                .orElseThrow(TiendaNoEncontradaException::new)
                .getId();
    }

    private static MedioPago medio(String nombre) {
        if (nombre == null) {
            throw new MediosPagoInvalidosException();
        }
        try {
            return MedioPago.valueOf(nombre);
        } catch (IllegalArgumentException medioDesconocido) {
            throw new MediosPagoInvalidosException();
        }
    }

    private static List<String> nombres(MediosPagoAceptados aceptados) {
        return aceptados.medios().stream().map(MedioPago::name).toList();
    }
}
