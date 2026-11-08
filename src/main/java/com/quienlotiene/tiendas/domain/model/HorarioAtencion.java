package com.quienlotiene.tiendas.domain.model;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Horario de atención de la tienda, una franja por día, en hora de Colombia (DECISIONES-BACKEND, sección 10). Un día
 * sin franja es un día cerrado. Un horario sin ningún día configurado no restringe: la tienda recibe solicitudes
 * siempre que tenga el interruptor en Abierto (HU-009, CP-TIE-009-01).
 */
public final class HorarioAtencion {

    public static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");

    private final Map<DayOfWeek, FranjaHoraria> franjasPorDia;

    private HorarioAtencion(Map<DayOfWeek, FranjaHoraria> franjasPorDia) {
        this.franjasPorDia = franjasPorDia;
    }

    public static HorarioAtencion sinConfigurar() {
        return new HorarioAtencion(new EnumMap<>(DayOfWeek.class));
    }

    /**
     * @throws HorarioInvalidoException si un día aparece dos veces
     */
    public static HorarioAtencion de(Collection<FranjaHoraria> franjas) {
        var franjasPorDia = new EnumMap<DayOfWeek, FranjaHoraria>(DayOfWeek.class);
        for (var franja : franjas) {
            if (franjasPorDia.putIfAbsent(franja.dia(), franja) != null) {
                throw new HorarioInvalidoException(
                        "el " + FranjaHoraria.nombreDelDia(franja.dia()) + " aparece dos veces");
            }
        }
        return new HorarioAtencion(franjasPorDia);
    }

    public boolean estaConfigurado() {
        return !franjasPorDia.isEmpty();
    }

    /** CP-TIE-009-01: con horario de 8:00 a 18:00, a las 19:00 (hora de Colombia) la tienda no atiende. */
    public boolean atiendeEn(Instant momento) {
        if (!estaConfigurado()) {
            return true;
        }
        var horaLocal = momento.atZone(ZONA_COLOMBIA);
        return Optional.ofNullable(franjasPorDia.get(horaLocal.getDayOfWeek()))
                .map(franja -> franja.incluye(horaLocal.toLocalTime()))
                .orElse(false);
    }

    /** Franjas de lunes a domingo. */
    public List<FranjaHoraria> franjas() {
        return franjasPorDia.values().stream()
                .sorted(Comparator.comparing(FranjaHoraria::dia))
                .toList();
    }

    /**
     * Una franja de un día. Abre antes de cerrar; la hora de cierre ya no cuenta como abierta.
     *
     * @throws HorarioInvalidoException si falta un dato o la apertura no es antes del cierre
     */
    public record FranjaHoraria(DayOfWeek dia, LocalTime abre, LocalTime cierra) {

        public FranjaHoraria {
            if (dia == null || abre == null || cierra == null) {
                throw new HorarioInvalidoException("cada día necesita hora de apertura y de cierre");
            }
            if (!abre.isBefore(cierra)) {
                throw new HorarioInvalidoException(
                        "el " + nombreDelDia(dia) + " la apertura debe ser antes del cierre");
            }
        }

        boolean incluye(LocalTime hora) {
            return !hora.isBefore(abre) && hora.isBefore(cierra);
        }

        static String nombreDelDia(DayOfWeek dia) {
            return switch (dia) {
                case MONDAY -> "lunes";
                case TUESDAY -> "martes";
                case WEDNESDAY -> "miércoles";
                case THURSDAY -> "jueves";
                case FRIDAY -> "viernes";
                case SATURDAY -> "sábado";
                case SUNDAY -> "domingo";
            };
        }
    }
}
