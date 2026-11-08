package com.quienlotiene.tiendas.domain.model;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * Medios de pago activos de una tienda (HU-012). Al crear la orden el cliente solo puede elegir uno de estos
 * (CP-TIE-012-01, diagrama 04 paso 3).
 */
public final class MediosPagoAceptados {

    private final Set<MedioPago> medios;

    private MediosPagoAceptados(Set<MedioPago> medios) {
        this.medios = medios;
    }

    /** Una tienda recién registrada todavía no configura sus medios de pago. */
    public static MediosPagoAceptados sinConfigurar() {
        return new MediosPagoAceptados(EnumSet.noneOf(MedioPago.class));
    }

    /**
     * @throws MediosPagoInvalidosException si no hay ninguno
     */
    public static MediosPagoAceptados de(Collection<MedioPago> medios) {
        if (medios == null || medios.isEmpty()) {
            throw new MediosPagoInvalidosException();
        }
        return new MediosPagoAceptados(EnumSet.copyOf(medios));
    }

    public boolean acepta(MedioPago medio) {
        return medios.contains(medio);
    }

    /** Copia ordenada: efectivo, transferencia, datáfono. */
    public Set<MedioPago> medios() {
        return medios.isEmpty() ? EnumSet.noneOf(MedioPago.class) : EnumSet.copyOf(medios);
    }
}
