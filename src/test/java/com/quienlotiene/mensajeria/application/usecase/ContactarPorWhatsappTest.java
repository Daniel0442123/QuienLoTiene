package com.quienlotiene.mensajeria.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.quienlotiene.mensajeria.domain.model.ContactoNoDisponibleException;
import com.quienlotiene.ordenes.api.ContactoDeOrden;
import com.quienlotiene.ordenes.api.OrdenesApi;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("HU-032")
@DisplayName("HU-032 Contactar por WhatsApp")
class ContactarPorWhatsappTest {

    private static final UUID USUARIO = UUID.randomUUID();
    private static final UUID ORDEN = UUID.randomUUID();

    @Mock
    OrdenesApi ordenesApi;

    @Test
    @Tag("CP-MEN-032-01")
    @DisplayName("CP-MEN-032-01 Con la orden activa entrega el nombre, el número y el enlace de la otra parte")
    void conOrdenActiva() {
        when(ordenesApi.obtenerContactoDeLaOtraParte(ORDEN, USUARIO))
                .thenReturn(new ContactoDeOrden(ORDEN, true, "Droguería Central", "3001112233", "Paracetamol"));

        var contacto = new ContactarPorWhatsapp(ordenesApi).ejecutar(USUARIO, ORDEN);

        assertThat(contacto.nombre()).isEqualTo("Droguería Central");
        assertThat(contacto.telefono()).isEqualTo("573001112233");
        assertThat(contacto.enlace()).startsWith("https://wa.me/573001112233");
        assertThat(contacto.toString()).doesNotContain("3001112233");
    }

    @Test
    @Tag("CP-MEN-032-02")
    @DisplayName("CP-MEN-032-02 Con la orden terminada el contacto ya no está disponible")
    void conOrdenTerminada() {
        when(ordenesApi.obtenerContactoDeLaOtraParte(ORDEN, USUARIO))
                .thenReturn(new ContactoDeOrden(ORDEN, false, "Droguería Central", "3001112233", "Paracetamol"));

        assertThatThrownBy(() -> new ContactarPorWhatsapp(ordenesApi).ejecutar(USUARIO, ORDEN))
                .isInstanceOf(ContactoNoDisponibleException.class);
    }
}
