package com.quienlotiene.tiendas.domain.model;

import java.util.Optional;

/**
 * Si la tienda lleva pedidos a domicilio, hasta qué distancia y con qué tarifa (HU-010, CU-17). El radio va en
 * metros, como el de las solicitudes, y no puede pasar de 10 km.
 */
public final class ConfiguracionDomicilio {

    public static final int RADIO_MINIMO_METROS = 500;
    public static final int RADIO_MAXIMO_METROS = 10_000;

    private final boolean haceDomicilio;
    private final int radioCoberturaEnMetros;
    private final TarifaDomicilio tarifa;

    private ConfiguracionDomicilio(boolean haceDomicilio, int radioCoberturaEnMetros, TarifaDomicilio tarifa) {
        this.haceDomicilio = haceDomicilio;
        this.radioCoberturaEnMetros = radioCoberturaEnMetros;
        this.tarifa = tarifa;
    }

    /** Solo para recoger en la tienda. */
    public static ConfiguracionDomicilio sinDomicilio() {
        return new ConfiguracionDomicilio(false, 0, null);
    }

    /**
     * @throws DomicilioInvalidoException si falta la tarifa o el radio no está entre 500 m y 10 km
     */
    public static ConfiguracionDomicilio conDomicilio(int radioCoberturaEnMetros, TarifaDomicilio tarifa) {
        if (tarifa == null) {
            throw new DomicilioInvalidoException("falta la tarifa");
        }
        if (radioCoberturaEnMetros < RADIO_MINIMO_METROS || radioCoberturaEnMetros > RADIO_MAXIMO_METROS) {
            throw new DomicilioInvalidoException("el radio de cobertura debe estar entre " + RADIO_MINIMO_METROS + " y "
                    + RADIO_MAXIMO_METROS + " metros");
        }
        return new ConfiguracionDomicilio(true, radioCoberturaEnMetros, tarifa);
    }

    public boolean cubre(int distanciaEnMetros) {
        return haceDomicilio && distanciaEnMetros <= radioCoberturaEnMetros;
    }

    /**
     * Costo del domicilio para un pedido (CU-17 paso 9).
     *
     * @throws FueraDeCoberturaException si no hace domicilio o el cliente está fuera del radio
     */
    public long calcularCostoEnPesos(long subtotalEnPesos, int distanciaEnMetros) {
        if (!cubre(distanciaEnMetros)) {
            throw new FueraDeCoberturaException();
        }
        return tarifa.calcularCostoEnPesos(subtotalEnPesos, distanciaEnMetros);
    }

    public boolean haceDomicilio() {
        return haceDomicilio;
    }

    public int radioCoberturaEnMetros() {
        return radioCoberturaEnMetros;
    }

    public Optional<TarifaDomicilio> tarifa() {
        return Optional.ofNullable(tarifa);
    }
}
