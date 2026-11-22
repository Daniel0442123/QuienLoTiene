package com.quienlotiene.identidad.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RNF-009 (NF-SEG-009): cada cuenta tiene como máximo {@value #MAXIMO_POR_MINUTO} intentos de inicio de sesión en
 * cualquier ventana de un minuto, acierte o no la frase. La cuenta se identifica por el correo escrito, en minúsculas y
 * sin espacios, para que el límite aplique también a un correo que no existe (no revela si la cuenta existe).
 *
 * <p>Los intentos viven en la memoria de la API: la aplicación corre en una sola instancia (D-01) y un reinicio solo
 * borra la ventana del último minuto.
 */
public final class IntentosInicioSesion {

    public static final int MAXIMO_POR_MINUTO = 5;
    public static final Duration VENTANA = Duration.ofMinutes(1);

    /** Por encima de este número de cuentas se barren las ventanas vencidas de todas. */
    private static final int CUENTAS_ANTES_DE_LIMPIAR = 10_000;

    private final Map<String, Deque<Instant>> intentosPorCuenta = new ConcurrentHashMap<>();

    /**
     * Anota un intento de la cuenta.
     *
     * @throws DemasiadosIntentosInicioSesionException si la cuenta ya tuvo {@value #MAXIMO_POR_MINUTO} intentos en el
     *     último minuto; ese intento no se cuenta
     */
    public void registrar(String correoEscrito, Instant ahora) {
        if (intentosPorCuenta.size() > CUENTAS_ANTES_DE_LIMPIAR) {
            limpiarVencidos(ahora);
        }
        var cuenta = correoEscrito == null ? "" : correoEscrito.strip().toLowerCase(Locale.ROOT);
        var intentos = intentosPorCuenta.computeIfAbsent(cuenta, clave -> new ArrayDeque<>());
        synchronized (intentos) {
            descartarVencidos(intentos, ahora);
            if (intentos.size() >= MAXIMO_POR_MINUTO) {
                var espera = Duration.between(ahora, intentos.peekFirst().plus(VENTANA));
                throw new DemasiadosIntentosInicioSesionException(espera);
            }
            intentos.addLast(ahora);
        }
    }

    private void limpiarVencidos(Instant ahora) {
        intentosPorCuenta.entrySet().removeIf(entrada -> {
            synchronized (entrada.getValue()) {
                descartarVencidos(entrada.getValue(), ahora);
                return entrada.getValue().isEmpty();
            }
        });
    }

    private static void descartarVencidos(Deque<Instant> intentos, Instant ahora) {
        var inicioVentana = ahora.minus(VENTANA);
        while (!intentos.isEmpty() && !intentos.peekFirst().isAfter(inicioVentana)) {
            intentos.removeFirst();
        }
    }

    /** Cuentas con intentos guardados (para las pruebas de la limpieza). */
    int cuentasRegistradas() {
        return intentosPorCuenta.size();
    }
}
