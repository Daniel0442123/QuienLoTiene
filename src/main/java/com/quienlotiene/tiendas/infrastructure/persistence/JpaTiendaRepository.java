package com.quienlotiene.tiendas.infrastructure.persistence;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.tiendas.domain.model.DocumentoVerificacion;
import com.quienlotiene.tiendas.domain.model.EstadoTienda;
import com.quienlotiene.tiendas.domain.model.HorarioAtencion;
import com.quienlotiene.tiendas.domain.model.Tienda;
import com.quienlotiene.tiendas.domain.model.TiendaCandidata;
import com.quienlotiene.tiendas.domain.model.TiendaNoEncontradaException;
import com.quienlotiene.tiendas.domain.model.TipoDocumento;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link TiendaRepository}. */
@Component
class JpaTiendaRepository implements TiendaRepository {

    private final EntityManager entityManager;
    private final TiendaJpaRepository tiendaJpaRepository;
    private final DocumentoVerificacionJpaRepository documentoJpaRepository;
    private final HorarioJpaRepository horarioJpaRepository;
    private final JdbcClient jdbcClient;
    private final Clock reloj;

    JpaTiendaRepository(
            EntityManager entityManager,
            TiendaJpaRepository tiendaJpaRepository,
            DocumentoVerificacionJpaRepository documentoJpaRepository,
            HorarioJpaRepository horarioJpaRepository,
            JdbcClient jdbcClient,
            Clock reloj) {
        this.entityManager = entityManager;
        this.tiendaJpaRepository = tiendaJpaRepository;
        this.documentoJpaRepository = documentoJpaRepository;
        this.horarioJpaRepository = horarioJpaRepository;
        this.jdbcClient = jdbcClient;
        this.reloj = reloj;
    }

    @Override
    public void guardarNueva(Tienda tienda, List<DocumentoVerificacion> documentos) {
        entityManager.persist(new TiendaEntity(
                tienda.getId(),
                tienda.getDuenoId(),
                tienda.getCategoriaId(),
                tienda.getNombre(),
                tienda.getTelefono(),
                tienda.getUbicacion().latitud(),
                tienda.getUbicacion().longitud(),
                tienda.getEstado().name(),
                tienda.estaAbierta(),
                tienda.getFechaRegistro()));
        for (var documento : documentos) {
            entityManager.persist(new DocumentoVerificacionEntity(
                    documento.id(),
                    documento.tiendaId(),
                    documento.tipo().name(),
                    documento.ruta(),
                    documento.tipoContenido(),
                    documento.tamanoEnBytes(),
                    documento.fechaSubida()));
        }
        // Si la base rechaza algo, que falle aquí y no al confirmar: así el caso de uso borra los archivos
        entityManager.flush();
    }

    @Override
    public Optional<Tienda> buscarPorId(UUID tiendaId) {
        return tiendaJpaRepository.findById(tiendaId).map(this::aDominio);
    }

    @Override
    public Optional<Tienda> buscarPorDueno(UUID duenoId) {
        return tiendaJpaRepository.findByDuenoId(duenoId).map(this::aDominio);
    }

    @Override
    public void guardarApertura(Tienda tienda) {
        var fila = tiendaJpaRepository.findById(tienda.getId()).orElseThrow(TiendaNoEncontradaException::new);
        fila.cambiarApertura(tienda.estaAbierta(), reloj.instant());
    }

    /**
     * SQL nativo permitido por D-23 (búsqueda por radio con PostGIS), siempre con parámetros. {@code ST_DWithin} sobre
     * {@code geography} mide en metros y usa el índice GiST de {@code ubicacion}.
     */
    @Override
    public List<TiendaCandidata> buscarCandidatasCerca(UUID categoriaId, Ubicacion punto, int radioEnMetros) {
        var distanciasPorTienda = jdbcClient
                .sql("""
                        select t.id,
                               st_distance(t.ubicacion, st_setsrid(st_makepoint(:longitud, :latitud), 4326)::geography)
                                   as distancia_metros
                        from tiendas.tienda t
                        where t.estado = 'APROBADA'
                          and t.abierta = true
                          and t.categoria_id = :categoriaId
                          and st_dwithin(t.ubicacion,
                                         st_setsrid(st_makepoint(:longitud, :latitud), 4326)::geography,
                                         :radioEnMetros)
                        order by distancia_metros
                        """)
                .param("longitud", punto.longitud())
                .param("latitud", punto.latitud())
                .param("categoriaId", categoriaId)
                .param("radioEnMetros", radioEnMetros)
                .query((fila, numeroFila) ->
                        new DistanciaTienda(fila.getObject("id", UUID.class), fila.getDouble("distancia_metros")))
                .list();
        return distanciasPorTienda.stream()
                .map(distancia -> new TiendaCandidata(
                        buscarPorId(distancia.tiendaId()).orElseThrow(TiendaNoEncontradaException::new),
                        distancia.metros()))
                .toList();
    }

    private record DistanciaTienda(UUID tiendaId, double metros) {}

    @Override
    public void guardarPerfil(Tienda tienda) {
        var ahora = reloj.instant();
        var fila = tiendaJpaRepository.findById(tienda.getId()).orElseThrow(TiendaNoEncontradaException::new);
        fila.actualizarPerfil(tienda.getNombre(), tienda.getTelefono(), ahora);
        horarioJpaRepository.borrarDeTienda(tienda.getId());
        for (var franja : tienda.getHorario().franjas()) {
            entityManager.persist(new HorarioEntity(
                    UUID.randomUUID(),
                    tienda.getId(),
                    (short) franja.dia().getValue(),
                    franja.abre(),
                    franja.cierra(),
                    ahora));
        }
    }

    @Override
    public List<Tienda> listarPorEstado(EstadoTienda estado) {
        return tiendaJpaRepository.findByEstadoOrderByCreadoEnAsc(estado.name()).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public List<DocumentoVerificacion> listarDocumentos(UUID tiendaId) {
        return documentoJpaRepository.findByTiendaIdOrderByTipoAsc(tiendaId).stream()
                .map(fila -> new DocumentoVerificacion(
                        fila.getId(),
                        fila.getTiendaId(),
                        TipoDocumento.valueOf(fila.getTipo()),
                        fila.getRuta(),
                        fila.getTipoContenido(),
                        fila.getTamanoBytes(),
                        fila.getCreadoEn()))
                .toList();
    }

    @Override
    public void guardarRevision(Tienda tienda) {
        var fila = tiendaJpaRepository.findById(tienda.getId()).orElseThrow(TiendaNoEncontradaException::new);
        fila.registrarRevision(
                tienda.getEstado().name(),
                tienda.getMotivoRechazo().orElse(null),
                tienda.getFechaRevision().orElseThrow());
    }

    private Tienda aDominio(TiendaEntity fila) {
        var horario = HorarioAtencion.de(horarioJpaRepository.findByTiendaIdOrderByDiaSemanaAsc(fila.getId()).stream()
                .map(franja -> new HorarioAtencion.FranjaHoraria(
                        DayOfWeek.of(franja.getDiaSemana()), franja.getAbre(), franja.getCierra()))
                .toList());
        return Tienda.reconstituir(
                fila.getId(),
                fila.getDuenoId(),
                fila.getCategoriaId(),
                fila.getNombre(),
                fila.getTelefono(),
                horario,
                new Ubicacion(fila.latitud(), fila.longitud()),
                EstadoTienda.valueOf(fila.getEstado()),
                fila.isAbierta(),
                fila.getCreadoEn(),
                fila.getMotivoRechazo(),
                fila.getFechaRevision());
    }
}
