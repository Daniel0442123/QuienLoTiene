package com.quienlotiene.tiendas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.tiendas.fixtures.ArchivosDePrueba;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-002")
@DisplayName("HU-002 Tienda y documentos")
class TiendaTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:30:00Z");
    private static final Ubicacion POPAYAN = new Ubicacion(2.4448, -76.6147);

    private static Tienda registrar(String nombre, UUID categoriaId, String telefono, Ubicacion ubicacion) {
        return Tienda.registrar(UUID.randomUUID(), UUID.randomUUID(), categoriaId, nombre, telefono, ubicacion, AHORA);
    }

    @Test
    @Tag("CP-TIE-002-01")
    @DisplayName("CP-TIE-002-01 La tienda registrada queda Pendiente y no entra en la difusión")
    void quedaPendienteYFueraDeLaDifusion() {
        // Cuando
        var tienda = registrar(" Droguería La 5 ", UUID.randomUUID(), " 3001234567 ", POPAYAN);

        // Entonces
        assertThat(tienda.getEstado()).isEqualTo(EstadoTienda.PENDIENTE);
        assertThat(tienda.estaAbierta()).isFalse();
        assertThat(tienda.puedeRecibirSolicitudes(AHORA)).isFalse();
        assertThat(tienda.getNombre()).isEqualTo("Droguería La 5");
        assertThat(tienda.getTelefono()).isEqualTo("3001234567");
        assertThat(tienda.getUbicacion()).isEqualTo(POPAYAN);
        assertThat(tienda.getFechaRegistro()).isEqualTo(AHORA);
        assertThat(tienda.getId()).isNotNull();
        assertThat(tienda.getDuenoId()).isNotNull();
        assertThat(tienda.getCategoriaId()).isNotNull();
    }

    @Test
    @DisplayName("Rechaza la tienda sin nombre, categoría, teléfono o ubicación, y dice qué falta")
    void rechazaLaTiendaIncompleta() {
        var categoria = UUID.randomUUID();
        assertThatThrownBy(() -> registrar(" ", categoria, "3001234567", POPAYAN))
                .isInstanceOf(TiendaIncompletaException.class)
                .hasMessageContaining("nombre")
                .extracting("codigo")
                .isEqualTo("TIE-001");
        assertThatThrownBy(() -> registrar("a".repeat(121), categoria, "3001234567", POPAYAN))
                .hasMessageContaining("120");
        assertThatThrownBy(() -> registrar("La 5", null, "3001234567", POPAYAN)).hasMessageContaining("categoría");
        assertThatThrownBy(() -> registrar("La 5", categoria, null, POPAYAN)).hasMessageContaining("teléfono");
        assertThatThrownBy(() -> registrar("La 5", categoria, "3001234567", null))
                .hasMessageContaining("ubicación");
    }

    @Test
    @Tag("CP-TIE-002-02")
    @DisplayName("CP-TIE-002-02 Sin el documento de existencia se rechaza e indica cuál falta")
    void rechazaElRegistroSinDocumento() {
        assertThatThrownBy(() -> ArchivoSubido.validar(TipoDocumento.RUT, null))
                .isInstanceOf(DocumentoFaltanteException.class)
                .hasMessageContaining("RUT")
                .extracting("codigo")
                .isEqualTo("TIE-002");
        assertThatThrownBy(() -> ArchivoSubido.validar(TipoDocumento.FOTO_LOCAL, new byte[0]))
                .isInstanceOf(DocumentoFaltanteException.class)
                .hasMessageContaining("foto del local");
    }

    @Test
    @DisplayName("Reconoce JPEG, PNG, WebP y PDF por su contenido")
    void reconoceLosFormatosPermitidos() {
        assertThat(ArchivoSubido.validar(TipoDocumento.FOTO_LOCAL, ArchivosDePrueba.unJpeg())
                        .tipoArchivo())
                .isEqualTo(TipoArchivo.JPEG);
        assertThat(ArchivoSubido.validar(TipoDocumento.FOTO_LOCAL, ArchivosDePrueba.unPng())
                        .tipoArchivo())
                .isEqualTo(TipoArchivo.PNG);
        assertThat(ArchivoSubido.validar(TipoDocumento.FOTO_LOCAL, ArchivosDePrueba.unWebp())
                        .tipoArchivo())
                .isEqualTo(TipoArchivo.WEBP);
        var rut = ArchivoSubido.validar(TipoDocumento.RUT, ArchivosDePrueba.unPdf());
        assertThat(rut.tipoArchivo()).isEqualTo(TipoArchivo.PDF);
        assertThat(rut.tipoArchivo().extension()).isEqualTo("pdf");
        assertThat(rut.tipoArchivo().tipoContenido()).isEqualTo("application/pdf");
        assertThat(rut.tamanoEnBytes()).isEqualTo(64);
        assertThat(rut.toString()).contains("PDF");
    }

    @Test
    @DisplayName("Rechaza un ejecutable disfrazado de PDF y un archivo de más de 5 MB")
    void rechazaArchivosNoPermitidos() {
        assertThatThrownBy(() -> ArchivoSubido.validar(TipoDocumento.RUT, ArchivosDePrueba.unEjecutableDisfrazado()))
                .isInstanceOf(ArchivoNoPermitidoException.class)
                .extracting("codigo")
                .isEqualTo("TIE-003");
        var pdfMuyGrande = new byte[ArchivoSubido.TAMANO_MAXIMO_EN_BYTES + 1];
        System.arraycopy(ArchivosDePrueba.unPdf(), 0, pdfMuyGrande, 0, 8);
        assertThatThrownBy(() -> ArchivoSubido.validar(TipoDocumento.RUT, pdfMuyGrande))
                .isInstanceOf(ArchivoNoPermitidoException.class)
                .hasMessageContaining("5 MB");
        assertThatThrownBy(() -> ArchivoSubido.validar(TipoDocumento.RUT, new byte[] {'R', 'I', 'F', 'F'}))
                .isInstanceOf(ArchivoNoPermitidoException.class);
    }
}
