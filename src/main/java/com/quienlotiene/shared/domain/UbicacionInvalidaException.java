package com.quienlotiene.shared.domain;

/** La latitud no está entre -90 y 90 o la longitud no está entre -180 y 180. */
public class UbicacionInvalidaException extends ReglaNegocioException {

    public UbicacionInvalidaException() {
        super(
                "GEN-009",
                "Ubicación inválida",
                "La ubicación no es válida. Activa el GPS o elige el punto en el mapa.",
                TipoError.DATOS_INVALIDOS);
    }
}
