package com.quienlotiene.tiendas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quienlotiene.tiendas.fixtures.ArchivosDePrueba;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("NF-SEG-012")
@DisplayName("NF-SEG-012 Las imágenes se guardan sin metadatos EXIF")
class LimpiadorMetadatosTest {

    private static String comoTexto(byte[] contenido) {
        return new String(contenido, StandardCharsets.ISO_8859_1);
    }

    @Test
    @DisplayName("JPEG: quita APP1 (EXIF) y APP13 (IPTC) y deja la imagen igual a la que no tenía metadatos")
    void limpiaElJpeg() {
        var foto = ArchivoSubido.validar(TipoDocumento.FOTO_LOCAL, ArchivosDePrueba.unJpegConExif());

        assertThat(foto.tipoArchivo()).isEqualTo(TipoArchivo.JPEG);
        assertThat(comoTexto(foto.contenido())).doesNotContain(ArchivosDePrueba.METADATO);
        assertThat(foto.contenido()).isEqualTo(ArchivosDePrueba.unJpeg());
    }

    @Test
    @DisplayName("PNG: quita los bloques eXIf y tEXt")
    void limpiaElPng() {
        var foto = ArchivoSubido.validar(TipoDocumento.FOTO_LOCAL, ArchivosDePrueba.unPngConExif());

        assertThat(comoTexto(foto.contenido())).doesNotContain(ArchivosDePrueba.METADATO);
        assertThat(foto.contenido()).isEqualTo(ArchivosDePrueba.unPng());
    }

    @Test
    @DisplayName("WebP: quita los bloques EXIF y XMP, apaga sus marcas en VP8X y corrige el tamaño RIFF")
    void limpiaElWebp() {
        var foto = ArchivoSubido.validar(TipoDocumento.FOTO_LOCAL, ArchivosDePrueba.unWebpConExif());

        assertThat(comoTexto(foto.contenido())).doesNotContain(ArchivosDePrueba.METADATO);
        assertThat(foto.contenido()).isEqualTo(ArchivosDePrueba.unWebp());
    }

    @Test
    @DisplayName("Un JPEG sin metadatos y un PDF quedan como llegaron")
    void dejaIgualLoQueNoTieneMetadatos() {
        assertThat(LimpiadorMetadatos.limpiar(TipoArchivo.JPEG, ArchivosDePrueba.unJpeg()))
                .isEqualTo(ArchivosDePrueba.unJpeg());
        assertThat(LimpiadorMetadatos.limpiar(TipoArchivo.PDF, ArchivosDePrueba.unPdf()))
                .isEqualTo(ArchivosDePrueba.unPdf());
    }

    @Test
    @DisplayName("JPEG con bytes de relleno y marcadores sin longitud se copia completo")
    void respetaRellenoYMarcadoresSinLongitud() {
        var jpeg = new byte[] {
            (byte) 0xFF,
            (byte) 0xD8,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xD0,
            (byte) 0xFF,
            (byte) 0x01,
            (byte) 0xFF,
            (byte) 0xD9
        };

        assertThat(LimpiadorMetadatos.limpiar(TipoArchivo.JPEG, jpeg)).isEqualTo(new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xD0, (byte) 0xFF, (byte) 0x01, (byte) 0xFF, (byte) 0xD9
        });
    }

    @Test
    @DisplayName("Una imagen con la estructura dañada se rechaza (TIE-003)")
    void rechazaLaEstructuraDanada() {
        var jpegCortado = Arrays.copyOf(ArchivosDePrueba.unJpegConExif(), 10);
        var pngCortado = Arrays.copyOf(ArchivosDePrueba.unPng(), 20);
        var webpCortado = Arrays.copyOf(ArchivosDePrueba.unWebp(), 18);

        for (var danado : new byte[][] {jpegCortado, pngCortado, webpCortado}) {
            assertThatThrownBy(() -> ArchivoSubido.validar(TipoDocumento.FOTO_LOCAL, danado))
                    .isInstanceOf(ArchivoNoPermitidoException.class)
                    .extracting("codigo")
                    .isEqualTo("TIE-003");
        }
    }
}
