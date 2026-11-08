package com.quienlotiene.tiendas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.quienlotiene.identidad.api.NuevoDuenoTienda;
import com.quienlotiene.identidad.api.RegistroUsuariosApi;
import com.quienlotiene.shared.domain.AlmacenArchivos;
import com.quienlotiene.tiendas.application.dto.RegistrarTiendaComando;
import com.quienlotiene.tiendas.domain.model.DocumentoFaltanteException;
import com.quienlotiene.tiendas.domain.model.DocumentoVerificacion;
import com.quienlotiene.tiendas.domain.model.EstadoTienda;
import com.quienlotiene.tiendas.domain.model.Tienda;
import com.quienlotiene.tiendas.domain.model.TipoDocumento;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import com.quienlotiene.tiendas.fixtures.ArchivosDePrueba;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
@Tag("HU-002")
@DisplayName("HU-002 Registrar tienda")
class RegistrarTiendaTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:30:00Z");

    @Mock
    RegistroUsuariosApi registroUsuarios;

    @Mock
    TiendaRepository tiendaRepository;

    @Mock
    AlmacenArchivos almacenArchivos;

    RegistrarTienda registrarTienda;

    @BeforeEach
    void configurar() {
        registrarTienda = new RegistrarTienda(
                registroUsuarios, tiendaRepository, almacenArchivos, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @AfterEach
    void limpiarSincronizacion() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private static RegistrarTiendaComando comando(byte[] fotoLocal, byte[] rut) {
        return new RegistrarTiendaComando(
                "Carlos Muñoz",
                "carlos@correo.com",
                "3001234567",
                "mi tienda vende de todo un poco",
                true,
                "1.0",
                "Droguería La 5",
                UUID.randomUUID(),
                2.4448,
                -76.6147,
                fotoLocal,
                rut);
    }

    @Test
    @Tag("CP-TIE-002-01")
    @DisplayName("CP-TIE-002-01 Con todos los datos, la foto y el RUT la tienda queda Pendiente")
    void registraLaTiendaPendienteConSusDocumentos() {
        // Dado
        var duenoId = UUID.randomUUID();
        when(registroUsuarios.registrarDuenoTienda(any())).thenReturn(duenoId);
        when(almacenArchivos.guardar(anyString(), eq("jpg"), any())).thenReturn("tiendas/x/foto.jpg");
        when(almacenArchivos.guardar(anyString(), eq("pdf"), any())).thenReturn("tiendas/x/rut.pdf");

        // Cuando
        var resultado = registrarTienda.ejecutar(comando(ArchivosDePrueba.unJpeg(), ArchivosDePrueba.unPdf()));

        // Entonces
        assertThat(resultado.estado()).isEqualTo("PENDIENTE");
        assertThat(resultado.nombre()).isEqualTo("Droguería La 5");
        var tiendaGuardada = ArgumentCaptor.forClass(Tienda.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DocumentoVerificacion>> documentos = ArgumentCaptor.forClass(List.class);
        verify(tiendaRepository).guardarNueva(tiendaGuardada.capture(), documentos.capture());
        assertThat(tiendaGuardada.getValue().getDuenoId()).isEqualTo(duenoId);
        assertThat(tiendaGuardada.getValue().getEstado()).isEqualTo(EstadoTienda.PENDIENTE);
        assertThat(documentos.getValue())
                .extracting(documento -> documento.tipo())
                .containsExactly(TipoDocumento.FOTO_LOCAL, TipoDocumento.RUT);
        assertThat(documentos.getValue())
                .allMatch(documento -> documento.tiendaId().equals(resultado.id()));
        verify(almacenArchivos).guardar(eq("tiendas/" + resultado.id()), eq("pdf"), any());
        var dueno = ArgumentCaptor.forClass(NuevoDuenoTienda.class);
        verify(registroUsuarios).registrarDuenoTienda(dueno.capture());
        assertThat(dueno.getValue().aceptaPoliticaDatos()).isTrue();
    }

    @Test
    @Tag("CP-TIE-002-02")
    @DisplayName("CP-TIE-002-02 Sin el RUT no crea la cuenta ni guarda archivos")
    void noCreaNadaSinElDocumento() {
        assertThatThrownBy(() -> registrarTienda.ejecutar(comando(ArchivosDePrueba.unJpeg(), null)))
                .isInstanceOf(DocumentoFaltanteException.class)
                .hasMessageContaining("RUT");
        verifyNoInteractions(registroUsuarios, almacenArchivos, tiendaRepository);
    }

    @Test
    @DisplayName("Si la transacción se revierte, borra los archivos que alcanzó a guardar")
    void borraLosArchivosSiLaTransaccionSeRevierte() {
        // Dado: una transacción activa
        TransactionSynchronizationManager.initSynchronization();
        when(registroUsuarios.registrarDuenoTienda(any())).thenReturn(UUID.randomUUID());
        when(almacenArchivos.guardar(anyString(), anyString(), any()))
                .thenReturn("tiendas/x/foto.jpg", "tiendas/x/rut.pdf");
        registrarTienda.ejecutar(comando(ArchivosDePrueba.unJpeg(), ArchivosDePrueba.unPdf()));

        // Cuando: la transacción termina con rollback
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(sincronizacion ->
                        sincronizacion.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        // Entonces
        verify(almacenArchivos).borrar("tiendas/x/foto.jpg");
        verify(almacenArchivos).borrar("tiendas/x/rut.pdf");
    }

    @Test
    @DisplayName("Si la transacción confirma, los archivos se quedan")
    void conservaLosArchivosSiLaTransaccionConfirma() {
        TransactionSynchronizationManager.initSynchronization();
        when(registroUsuarios.registrarDuenoTienda(any())).thenReturn(UUID.randomUUID());
        when(almacenArchivos.guardar(anyString(), anyString(), any())).thenReturn("tiendas/x/archivo");
        registrarTienda.ejecutar(comando(ArchivosDePrueba.unPng(), ArchivosDePrueba.unPdf()));

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(sincronizacion -> sincronizacion.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));

        verify(almacenArchivos, never()).borrar(any());
    }
}
