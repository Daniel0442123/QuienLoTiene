package com.quienlotiene.ordenes.domain.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Máquina de estados de la orden (diagrama 05, patrón State del diagrama 04). La tienda avanza un paso a la vez; el
 * cliente cierra con «Recibí mi pedido». CANCELADO (HU-029) y VENCIDA (HU-025) los deciden {@link Orden#cancelarPorCliente},
 * {@link Orden#cancelarPorTienda} y {@link Orden#vencerReserva}.
 */
public enum EstadoOrden {
    CONFIRMADO("confirmada"),
    PREPARANDO("en preparación"),
    EN_CAMINO("en camino"),
    LISTO_PARA_RECOGER("lista para recoger"),
    ENTREGADO("entregada"),
    RESUELTA("resuelta"),
    CANCELADO("cancelada"),
    VENCIDA("vencida");

    private static final Set<EstadoOrden> ACTIVOS =
            EnumSet.of(CONFIRMADO, PREPARANDO, EN_CAMINO, LISTO_PARA_RECOGER, ENTREGADO);

    private final String textoParaUsuario;

    EstadoOrden(String textoParaUsuario) {
        this.textoParaUsuario = textoParaUsuario;
    }

    /** Mientras está activa, la tienda ve los datos del cliente y ambos pueden escribirse (HU-024, HU-032). */
    public boolean esActivo() {
        return ACTIVOS.contains(this);
    }

    /** Cambios que hace la tienda (HU-027): solo el paso siguiente válido según la modalidad. */
    public boolean tiendaPuedePasarA(EstadoOrden siguiente, ModalidadEntrega modalidad) {
        return switch (this) {
            case CONFIRMADO -> siguiente == PREPARANDO;
            case PREPARANDO -> siguiente == (modalidad == ModalidadEntrega.DOMICILIO ? EN_CAMINO : LISTO_PARA_RECOGER);
            case EN_CAMINO, LISTO_PARA_RECOGER -> siguiente == ENTREGADO;
            default -> false;
        };
    }

    String textoParaUsuario() {
        return textoParaUsuario;
    }
}
