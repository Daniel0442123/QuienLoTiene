import { zodResolver } from '@hookform/resolvers/zod'
import { Lock, Send } from 'lucide-react'
import { useEffect, useRef } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { z } from 'zod'
import { EsqueletoLista, EstadoError, EstadoVacio } from '@/components/Estados'
import { Boton } from '@/components/ui/Boton'
import { cn } from '@/components/ui/utilidades'
import { esErrorApi } from '@/services/errores'
import { formatearHora } from '@/services/formato'
import { mensajeParaUsuario } from '@/services/mensajes-error'
import { useChat, useEnviarMensaje } from './api'
import { textosMensajeria as textos } from './textos'

const LARGO_MAXIMO = 1000
const ID_CAMPO = 'chat-mensaje'

const esquemaMensaje = z.object({
  texto: z.string().trim().min(1, textos.mensajeVacio).max(LARGO_MAXIMO, textos.mensajeLargo),
})
type DatosMensaje = z.infer<typeof esquemaMensaje>

/**
 * HU-033 (mockup 02): los mensajes del pedido y el campo para escribir. La lista es una región `role="log"`: el
 * lector de pantalla lee cada mensaje que llega sin mover el foco (WCAG 4.1.3). Cada burbuja dice en texto quién lo
 * escribió, no solo con la alineación o el color. Con el pedido terminado (soloLectura o MEN-002) no se puede escribir.
 */
export function ChatDelPedido({ ordenId, otraParte }: { ordenId: string; otraParte: string }) {
  const chat = useChat(ordenId)
  const envio = useEnviarMensaje(ordenId)
  const final = useRef<HTMLLIElement>(null)
  const cantidad = chat.data?.mensajes.length ?? 0
  const {
    register,
    control,
    handleSubmit,
    reset,
    setFocus,
    formState: { errors },
  } = useForm<DatosMensaje>({
    resolver: zodResolver(esquemaMensaje),
    mode: 'onSubmit',
    reValidateMode: 'onSubmit',
    defaultValues: { texto: '' },
  })
  const largo = useWatch({ control, name: 'texto' }).length

  // El mensaje más reciente queda a la vista (jsdom no implementa scrollIntoView)
  useEffect(() => {
    final.current?.scrollIntoView?.({ block: 'end' })
  }, [cantidad])

  if (chat.isPending) return <EsqueletoLista filas={3} etiqueta={textos.cargando} />
  if (chat.isError) return <EstadoError error={chat.error} alReintentar={() => void chat.refetch()} />

  const { mensajes, soloLectura } = chat.data
  const cerradoAlEnviar = esErrorApi(envio.error) && envio.error.codigo === 'MEN-002'
  const errorEnvio = envio.error && !cerradoAlEnviar ? envio.error : null

  return (
    <div className="flex flex-col gap-4">
      {mensajes.length === 0 ? (
        <EstadoVacio titulo={textos.sinMensajes}>{soloLectura ? null : textos.sinMensajesAyuda}</EstadoVacio>
      ) : null}
      {/* La región viva va afuera de la lista para no quitarle a <ol> su papel de lista */}
      <div role="log" aria-label={textos.etiquetaMensajes}>
        <ol className="flex flex-col gap-3">
          {mensajes.map((mensaje, indice) => {
            // El que no es mío siempre es de la otra parte del pedido
            const quien = mensaje.esMio ? textos.dijiste : textos.dijo(otraParte)
            const hora = formatearHora(mensaje.fecha)
            return (
              <li
                key={mensaje.id}
                ref={indice === mensajes.length - 1 ? final : undefined}
                className={cn(
                  'flex max-w-[85%] flex-col gap-1 rounded-control px-3 py-2',
                  mensaje.esMio ? 'self-end bg-primario text-primario-texto' : 'self-start bg-superficie text-texto',
                )}
              >
                <span className="sr-only">{quien}</span>
                <p className="break-words whitespace-pre-line">{mensaje.texto}</p>
                <p className={cn('text-sm', mensaje.esMio ? 'text-primario-texto' : 'text-texto-suave')}>
                  <span className="sr-only">{textos.hora(hora)}</span>
                  <span aria-hidden="true">{hora}</span>
                </p>
              </li>
            )
          })}
        </ol>
      </div>

      {soloLectura || cerradoAlEnviar ? (
        <div className="flex items-start gap-3 rounded-control border border-borde bg-superficie p-3">
          <Lock aria-hidden="true" className="mt-0.5 size-5 shrink-0 text-texto-suave" />
          <div className="flex flex-col gap-1">
            <p role={cerradoAlEnviar ? 'alert' : undefined} className="font-semibold">
              {textos.soloLectura}
            </p>
            <p className="text-texto-suave">{textos.soloLecturaAyuda}</p>
          </div>
        </div>
      ) : (
        <form
          noValidate
          onSubmit={handleSubmit(({ texto }) =>
            envio.mutate(texto, {
              onSuccess: () => {
                reset()
                // Se sigue escribiendo sin volver a buscar el campo (después de que el botón deja de estar ocupado)
                requestAnimationFrame(() => setFocus('texto'))
              },
            }),
          )}
          className="sticky bottom-0 flex flex-col gap-1 border-t border-borde bg-fondo pt-3"
        >
          <label htmlFor={ID_CAMPO} className="font-semibold">
            {textos.etiquetaEscribir}
          </label>
          <div className="flex gap-2">
            <input
              id={ID_CAMPO}
              type="text"
              autoComplete="off"
              enterKeyHint="send"
              maxLength={LARGO_MAXIMO + 50}
              placeholder={textos.placeholder}
              aria-invalid={errors.texto ? true : undefined}
              aria-describedby={[`${ID_CAMPO}-contador`, errors.texto ? `${ID_CAMPO}-error` : null]
                .filter(Boolean)
                .join(' ')}
              className={cn(
                'min-h-tactil min-w-0 flex-1 rounded-control border border-borde bg-fondo px-3 text-base',
                errors.texto && 'border-peligro',
              )}
              {...register('texto')}
            />
            <Boton type="submit" enviando={envio.isPending} textoEnviando={textos.enviando}>
              <Send aria-hidden="true" className="size-5" />
              {textos.botonEnviar}
            </Boton>
          </div>
          <p id={`${ID_CAMPO}-contador`} className="text-sm text-texto-suave">
            {textos.contador(largo)}
          </p>
          {errors.texto ? (
            <p id={`${ID_CAMPO}-error`} className="text-sm font-medium text-peligro">
              {errors.texto.message}
            </p>
          ) : null}
          {errorEnvio ? (
            <p role="alert" className="font-medium text-peligro">
              {esErrorApi(errorEnvio) ? mensajeParaUsuario(errorEnvio) : textos.errorEnviar}
            </p>
          ) : null}
        </form>
      )}
    </div>
  )
}
