package com.quienlotiene.tiendas.domain.model;

/**
 * Cómo cobra la tienda el domicilio (patrón Strategy, diagrama 23). HU-010 trae la tarifa fija; "gratis desde un
 * monto" y "por distancia" llegan con HU-011. El mismo cálculo se usa al responder una solicitud y al crear la orden.
 */
public sealed interface TarifaDomicilio permits TarifaDomicilio.TarifaFija {

    /** Pesos enteros (D-14): el peso colombiano no usa centavos en la práctica. */
    long calcularCostoEnPesos(long subtotalEnPesos, int distanciaEnMetros);

    String tipo();

    /**
     * El mismo valor sin importar el subtotal ni la distancia (CP-TIE-010-01).
     *
     * @throws DomicilioInvalidoException si el valor es negativo (CP-TIE-010-02)
     */
    record TarifaFija(long valorEnPesos) implements TarifaDomicilio {

        public static final String TIPO = "FIJA";

        public TarifaFija {
            if (valorEnPesos < 0) {
                throw new DomicilioInvalidoException("la tarifa no puede ser negativa");
            }
        }

        @Override
        public long calcularCostoEnPesos(long subtotalEnPesos, int distanciaEnMetros) {
            return valorEnPesos;
        }

        @Override
        public String tipo() {
            return TIPO;
        }
    }
}
