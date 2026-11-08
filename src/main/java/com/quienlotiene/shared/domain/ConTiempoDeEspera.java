package com.quienlotiene.shared.domain;

import java.time.Duration;

/**
 * Lo implementan las excepciones de límites de uso: el manejador global envía este tiempo en la cabecera
 * {@code Retry-After} para que el cliente sepa cuándo puede volver a intentar.
 */
public interface ConTiempoDeEspera {

    Duration tiempoDeEspera();
}
