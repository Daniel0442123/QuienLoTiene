package com.quienlotiene.mensajeria.application.usecase;

import com.quienlotiene.mensajeria.application.dto.ContactoWhatsapp;
import com.quienlotiene.mensajeria.domain.model.ContactoNoDisponibleException;
import com.quienlotiene.mensajeria.domain.model.EnlaceWhatsapp;
import com.quienlotiene.ordenes.api.OrdenesApi;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * HU-032 Contactar por WhatsApp (CU-12, diagrama 18): el teléfono de la otra parte solo se entrega con la orden
 * activa (CP-MEN-032-01); antes de elegir no hay orden y por eso no hay número (CP-MEN-032-02).
 */
@Service
public class ContactarPorWhatsapp {

    private final OrdenesApi ordenesApi;

    ContactarPorWhatsapp(OrdenesApi ordenesApi) {
        this.ordenesApi = ordenesApi;
    }

    /**
     * @throws ContactoNoDisponibleException si la orden ya no está activa
     */
    public ContactoWhatsapp ejecutar(UUID usuarioId, UUID ordenId) {
        var contacto = ordenesApi.obtenerContactoDeLaOtraParte(ordenId, usuarioId);
        if (!contacto.esActiva()) {
            throw new ContactoNoDisponibleException();
        }
        var enlace = EnlaceWhatsapp.para(contacto.telefono(), contacto.ordenId(), contacto.producto());
        return new ContactoWhatsapp(contacto.nombre(), enlace.telefono(), enlace.enlace());
    }
}
