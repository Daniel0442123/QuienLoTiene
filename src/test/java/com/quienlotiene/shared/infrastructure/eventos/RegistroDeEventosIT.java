package com.quienlotiene.shared.infrastructure.eventos;

import static org.assertj.core.api.Assertions.assertThat;

import com.quienlotiene.ContenedoresDePrueba;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * NF-FIA-015 (RNF-015): un evento cuyo procesamiento se corta (la API se cae mientras el listener trabaja) queda en
 * {@code event_publication} sin {@code completion_date} y se procesa cuando se reenvían los pendientes, que es lo que
 * hace Spring Modulith al arrancar con {@code republish-outstanding-events-on-restart: true}. El reinicio se simula
 * llamando el mismo reenvío ({@link IncompleteEventPublications}); un reinicio real del contexto no cabe en la suite.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import({ContenedoresDePrueba.class, RegistroDeEventosIT.ListenerQueFallaUnaVez.class})
@Testcontainers(disabledWithoutDocker = true)
@Tag("NF-FIA-015")
@DisplayName("NF-FIA-015 Los eventos pendientes se reprocesan después de un reinicio")
class RegistroDeEventosIT {

    /** Evento de prueba; se guarda en JSON en el registro. */
    public record EventoDePrueba(UUID id) {}

    /** Listener que falla la primera vez que recibe cada evento, como si la API se cayera en ese momento. */
    @TestConfiguration(proxyBeanMethods = false)
    static class ListenerQueFallaUnaVez {

        static final AtomicInteger INTENTOS = new AtomicInteger();
        static final AtomicInteger PROCESADOS = new AtomicInteger();

        @Bean
        Receptor receptorDePrueba() {
            return new Receptor();
        }

        static class Receptor {

            @TransactionalEventListener
            void procesar(EventoDePrueba evento) {
                if (INTENTOS.incrementAndGet() == 1) {
                    throw new IllegalStateException("Caída simulada mientras se procesaba " + evento.id());
                }
                PROCESADOS.incrementAndGet();
            }
        }
    }

    @Autowired
    ApplicationEventPublisher publicador;

    @Autowired
    TransactionTemplate transaccion;

    @Autowired
    IncompleteEventPublications pendientes;

    @Autowired
    JdbcClient jdbcClient;

    @Autowired
    Environment entorno;

    private long pendientesDe(UUID id) {
        return jdbcClient
                .sql("select count(*) from event_publication where serialized_event like :id"
                        + " and completion_date is null")
                .param("id", "%" + id + "%")
                .query(Long.class)
                .single();
    }

    private long completadosDe(UUID id) {
        return jdbcClient
                .sql("select count(*) from event_publication where serialized_event like :id"
                        + " and completion_date is not null")
                .param("id", "%" + id + "%")
                .query(Long.class)
                .single();
    }

    @Test
    @DisplayName("El evento que falló queda pendiente y al reenviar los pendientes se procesa: 0 eventos perdidos")
    void noSePierdenEventos() {
        // Dado: la app reenvía los pendientes al arrancar
        assertThat(entorno.getProperty("spring.modulith.events.republish-outstanding-events-on-restart"))
                .isEqualTo("true");
        ListenerQueFallaUnaVez.INTENTOS.set(0);
        ListenerQueFallaUnaVez.PROCESADOS.set(0);
        var evento = new EventoDePrueba(UUID.randomUUID());

        // Cuando: se publica y el listener se cae
        try {
            transaccion.executeWithoutResult(estado -> publicador.publishEvent(evento));
        } catch (IllegalStateException caidaSimulada) {
            // Según la versión de Spring, la falla del listener llega o no hasta quien publica
        }

        // Entonces: quedó registrado y sin completar
        assertThat(ListenerQueFallaUnaVez.PROCESADOS).hasValue(0);
        assertThat(pendientesDe(evento.id())).isEqualTo(1);

        // Cuando: «reinicio», se reenvían los pendientes
        pendientes.resubmitIncompletePublications(
                publicacion -> publicacion.getEvent().equals(evento));

        // Entonces: se procesó una vez y ya no queda pendiente
        assertThat(ListenerQueFallaUnaVez.PROCESADOS).hasValue(1);
        assertThat(pendientesDe(evento.id())).isZero();
        assertThat(completadosDe(evento.id())).isEqualTo(1);
    }
}
