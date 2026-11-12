import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router'
import { useAnunciar } from '@/components/RegionAnuncios'
import { Boton } from '@/components/ui/Boton'
import { cerrarSesion } from '@/services/sesion'
import { textosIdentidad as textos } from './textos'

/**
 * Cierra la sesión (HU-003): revoca el refresco en el backend, borra el token de memoria y las consultas guardadas
 * (tienen datos personales, Ley 1581) y lleva a entrar. Aunque el backend no responda, en este dispositivo se cierra.
 */
export function BotonCerrarSesion() {
  const clienteConsultas = useQueryClient()
  const navegar = useNavigate()
  const anunciar = useAnunciar()

  const salida = useMutation({
    mutationFn: () => cerrarSesion().catch(() => undefined),
    onSettled: () => {
      clienteConsultas.clear()
      anunciar(textos.sesionCerrada)
      navegar('/entrar', { replace: true })
    },
  })

  return (
    <Boton
      type="button"
      variante="secundario"
      enviando={salida.isPending}
      textoEnviando={textos.botonCerrarSesion}
      onClick={() => salida.mutate()}
    >
      {textos.botonCerrarSesion}
    </Boton>
  )
}
