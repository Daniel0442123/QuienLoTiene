import { zodResolver } from '@hookform/resolvers/zod'
import { Download, ExternalLink } from 'lucide-react'
import { useId, useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { cn } from '@/components/ui/utilidades'
import { esErrorApi } from '@/services/errores'
import { formatearFechaHora, formatearTamano } from '@/services/formato'
import {
  mensajeAdministracion,
  useAprobarTienda,
  useDescargarDocumento,
  useRechazarTienda,
  type TiendaParaRevision,
} from './api'
import { esquemaRechazo, LARGO_MAXIMO_MOTIVO, type DatosRechazo } from './esquemas'
import { textosAdministracion as textos } from './textos'

type Paso = 'inicio' | 'confirmarAprobacion' | 'rechazar'

/** Formulario del motivo del rechazo, dentro de la tarjeta (sin diálogo: se ve junto a la tienda que se rechaza). */
function FormularioRechazo({
  tienda,
  alCancelar,
  alRechazar,
}: {
  tienda: TiendaParaRevision
  alCancelar: () => void
  alRechazar: (mensaje: string) => void
}) {
  const rechazo = useRechazarTienda()
  const idMotivo = useId()
  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<DatosRechazo>({ resolver: zodResolver(esquemaRechazo), mode: 'onTouched', defaultValues: { motivo: '' } })
  const largo = useWatch({ control, name: 'motivo' }).length

  return (
    <form
      noValidate
      onSubmit={handleSubmit(({ motivo }) =>
        rechazo.mutate(
          { tiendaId: tienda.id, motivo },
          {
            onSuccess: () => alRechazar(textos.rechazada(tienda.nombre)),
            // Otra persona ya la revisó: la lista se recarga y esta tarjeta puede desaparecer, así que se avisa arriba
            onError: (error) => {
              if (esErrorApi(error) && error.codigo === 'TIE-004') alRechazar(mensajeAdministracion(error))
            },
          },
        ),
      )}
      className="flex flex-col gap-3 rounded-control border border-borde p-3"
    >
      <div className="flex flex-col gap-1">
        <label htmlFor={idMotivo} className="font-medium">
          {textos.etiquetaMotivo}
        </label>
        <p id={`${idMotivo}-pista`} className="text-sm text-texto-suave">
          {textos.pistaMotivo} {textos.contadorMotivo(largo)}
        </p>
        <textarea
          id={idMotivo}
          rows={3}
          maxLength={LARGO_MAXIMO_MOTIVO + 50}
          // El campo aparece porque la persona pidió rechazar: el foco va directo a escribir el motivo
          // eslint-disable-next-line jsx-a11y/no-autofocus
          autoFocus
          aria-invalid={errors.motivo ? true : undefined}
          aria-describedby={[`${idMotivo}-pista`, errors.motivo ? `${idMotivo}-error` : null].filter(Boolean).join(' ')}
          className={cn(
            'rounded-control border border-borde bg-fondo p-3 text-base',
            errors.motivo && 'border-peligro',
          )}
          {...register('motivo')}
        />
        {errors.motivo ? (
          <p id={`${idMotivo}-error`} className="text-sm font-medium text-peligro">
            {errors.motivo.message}
          </p>
        ) : null}
      </div>
      {rechazo.error ? (
        <p role="alert" className="text-sm font-medium text-peligro">
          {mensajeAdministracion(rechazo.error)}
        </p>
      ) : null}
      <div className="flex flex-wrap gap-2">
        <Boton type="submit" variante="peligro" enviando={rechazo.isPending} textoEnviando={textos.botonRechazando}>
          {textos.botonConfirmarRechazo}
        </Boton>
        <Boton type="button" variante="secundario" onClick={alCancelar}>
          {textos.botonCancelar}
        </Boton>
      </div>
    </form>
  )
}

/**
 * Una tienda para revisar (HU-036, CU-26): datos, ubicación, documentos para descargar y, si está PENDIENTE,
 * aprobar (con confirmación en la misma tarjeta) o rechazar con motivo.
 */
export function TarjetaTiendaRevision({
  tienda,
  nombreCategoria,
  alRevisar,
}: {
  tienda: TiendaParaRevision
  nombreCategoria: string
  alRevisar: (mensaje: string) => void
}) {
  const anunciar = useAnunciar()
  const aprobacion = useAprobarTienda()
  const descarga = useDescargarDocumento()
  const [paso, setPaso] = useState<Paso>('inicio')
  const idTitulo = useId()

  const terminar = (mensaje: string) => {
    anunciar(mensaje)
    alRevisar(mensaje)
  }

  return (
    <article aria-labelledby={idTitulo} className="flex flex-col gap-3 rounded-control border border-borde p-4">
      <div className="flex flex-col gap-1">
        <h2 id={idTitulo} className="text-lg font-semibold break-words">
          {tienda.nombre}
        </h2>
        <p className="text-texto-suave">
          {nombreCategoria} · {textos.estadoSingular[tienda.estado]}
        </p>
        <p className="text-sm">{textos.telefono(tienda.telefono)}</p>
        <p className="text-sm text-texto-suave">{textos.registrada(formatearFechaHora(tienda.fechaRegistro))}</p>
        {tienda.fechaRevision ? (
          <p className="text-sm text-texto-suave">{textos.revisada(formatearFechaHora(tienda.fechaRevision))}</p>
        ) : null}
        {tienda.motivoRechazo ? <p className="font-medium">{textos.motivoRechazo(tienda.motivoRechazo)}</p> : null}
        <a
          href={`https://www.openstreetmap.org/?mlat=${tienda.latitud}&mlon=${tienda.longitud}#map=18/${tienda.latitud}/${tienda.longitud}`}
          target="_blank"
          rel="noreferrer"
          className="inline-flex min-h-tactil items-center gap-1 self-start text-primario underline underline-offset-4 hover:no-underline"
        >
          {textos.verEnMapa}
          <ExternalLink aria-hidden="true" className="size-4" />
          <span className="sr-only">{textos.abreEnOtraPestana}</span>
        </a>
      </div>

      <section className="flex flex-col gap-2">
        <h3 className="font-medium">{textos.documentos}</h3>
        {tienda.documentos.length === 0 ? (
          <p className="text-sm text-texto-suave">{textos.sinDocumentos}</p>
        ) : (
          <ul className="flex flex-col gap-2">
            {tienda.documentos.map((documento) => {
              const descargandoEste = descarga.isPending && descarga.variables?.documento.id === documento.id
              const detalle = `${documento.tipoContenido.split('/')[1]?.toUpperCase() ?? ''}, ${formatearTamano(documento.tamanoEnBytes)}`
              return (
                <li key={documento.id}>
                  <Boton
                    type="button"
                    variante="secundario"
                    className="w-full justify-start text-left"
                    enviando={descargandoEste}
                    textoEnviando={textos.descargando}
                    onClick={() => descarga.mutate({ tiendaId: tienda.id, documento })}
                  >
                    <Download aria-hidden="true" className="size-5 shrink-0" />
                    {textos.descargar(textos.tipoDocumento[documento.tipo], detalle)}
                  </Boton>
                </li>
              )
            })}
          </ul>
        )}
        {descarga.error ? (
          <p role="alert" className="text-sm font-medium text-peligro">
            {textos.errorDescarga}
          </p>
        ) : null}
      </section>

      {tienda.estado === 'PENDIENTE' ? (
        <div className="flex flex-col gap-3">
          {paso === 'inicio' ? (
            <div className="flex flex-wrap gap-2">
              <Boton type="button" onClick={() => setPaso('confirmarAprobacion')}>
                {textos.botonAprobar}
              </Boton>
              <Boton type="button" variante="secundario" onClick={() => setPaso('rechazar')}>
                {textos.botonRechazar}
              </Boton>
            </div>
          ) : null}

          {paso === 'confirmarAprobacion' ? (
            <div className="flex flex-col gap-2 rounded-control border border-borde p-3">
              <p className="font-medium">{textos.confirmarAprobar(tienda.nombre)}</p>
              <div className="flex flex-wrap gap-2">
                <Boton
                  type="button"
                  // eslint-disable-next-line jsx-a11y/no-autofocus
                  autoFocus
                  enviando={aprobacion.isPending}
                  textoEnviando={textos.botonAprobando}
                  onClick={() =>
                    aprobacion.mutate(tienda.id, {
                      onSuccess: () => terminar(textos.aprobada(tienda.nombre)),
                      onError: (error) => {
                        if (esErrorApi(error) && error.codigo === 'TIE-004') terminar(mensajeAdministracion(error))
                      },
                    })
                  }
                >
                  {textos.botonConfirmarAprobar}
                </Boton>
                <Boton type="button" variante="secundario" onClick={() => setPaso('inicio')}>
                  {textos.botonCancelar}
                </Boton>
              </div>
              {aprobacion.error ? (
                <p role="alert" className="text-sm font-medium text-peligro">
                  {mensajeAdministracion(aprobacion.error)}
                </p>
              ) : null}
            </div>
          ) : null}

          {paso === 'rechazar' ? (
            <FormularioRechazo tienda={tienda} alCancelar={() => setPaso('inicio')} alRechazar={terminar} />
          ) : null}
        </div>
      ) : null}
    </article>
  )
}
