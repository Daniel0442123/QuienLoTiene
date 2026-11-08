package com.quienlotiene.tiendas.application.usecase;

import com.quienlotiene.tiendas.application.dto.Domicilio;
import com.quienlotiene.tiendas.domain.model.ConfiguracionDomicilio;
import com.quienlotiene.tiendas.domain.model.DomicilioInvalidoException;
import com.quienlotiene.tiendas.domain.model.TarifaDomicilio;
import com.quienlotiene.tiendas.domain.model.TiendaNoEncontradaException;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-010 Configurar domicilio con tarifa fija (CU-17): consulta, cambia y simula el costo del domicilio de la tienda
 * del usuario.
 */
@Slf4j
@Service
public class ConfigurarDomicilio {

    private final TiendaRepository tiendaRepository;

    ConfigurarDomicilio(TiendaRepository tiendaRepository) {
        this.tiendaRepository = tiendaRepository;
    }

    @Transactional(readOnly = true)
    public Domicilio consultar(UUID duenoId) {
        return aDomicilio(tiendaRepository.buscarDomicilio(tiendaDe(duenoId)));
    }

    @Transactional
    public Domicilio guardar(UUID duenoId, Domicilio domicilio) {
        var tiendaId = tiendaDe(duenoId);
        var configuracion = aConfiguracion(domicilio);
        tiendaRepository.guardarDomicilio(tiendaId, configuracion);
        log.info("Tienda {} actualizó su domicilio", tiendaId);
        return aDomicilio(configuracion);
    }

    /** CU-17 pasos 7 a 10: cuánto pagaría un cliente con ese subtotal y a esa distancia. */
    @Transactional(readOnly = true)
    public long simularCostoEnPesos(UUID duenoId, long subtotalEnPesos, int distanciaEnMetros) {
        return tiendaRepository
                .buscarDomicilio(tiendaDe(duenoId))
                .calcularCostoEnPesos(subtotalEnPesos, distanciaEnMetros);
    }

    private UUID tiendaDe(UUID duenoId) {
        return tiendaRepository
                .buscarPorDueno(duenoId)
                .orElseThrow(TiendaNoEncontradaException::new)
                .getId();
    }

    private static ConfiguracionDomicilio aConfiguracion(Domicilio domicilio) {
        if (!domicilio.haceDomicilio()) {
            return ConfiguracionDomicilio.sinDomicilio();
        }
        if (domicilio.radioCoberturaEnMetros() == null || domicilio.tarifaFijaEnPesos() == null) {
            throw new DomicilioInvalidoException("con domicilio activo faltan el radio o la tarifa");
        }
        return ConfiguracionDomicilio.conDomicilio(
                domicilio.radioCoberturaEnMetros(), new TarifaDomicilio.TarifaFija(domicilio.tarifaFijaEnPesos()));
    }

    private static Domicilio aDomicilio(ConfiguracionDomicilio configuracion) {
        if (!configuracion.haceDomicilio()) {
            return new Domicilio(false, null, null, null);
        }
        var tarifa = (TarifaDomicilio.TarifaFija) configuracion.tarifa().orElseThrow();
        return new Domicilio(true, configuracion.radioCoberturaEnMetros(), tarifa.tipo(), tarifa.valorEnPesos());
    }
}
