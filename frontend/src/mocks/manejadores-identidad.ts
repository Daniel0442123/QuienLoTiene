import { http, HttpResponse } from 'msw'
import { base, CODIGO_TOTP_DE_PRUEBA, VERSION_POLITICA_VIGENTE, type UsuarioSimulado } from './datos'
import {
  autorizar,
  CORREO_VALIDO,
  leerJson,
  problema,
  respuestaSesion,
  TELEFONO_VALIDO,
  vacio,
  validar,
} from './utilidades'

/** Identidad: registro de cliente, inicio y cierre de sesión, segundo factor y perfil (HU-001, 003, 004, 005, 007). */

const RUTA = '/api/v1/auth'
const CINCO_MINUTOS_MS = 5 * 60 * 1000
const FRASES_COMUNES = ['contraseña contraseña', 'password password', '123456789012345', 'qwertyuiopasdfgh']

/** Reglas de la frase que el backend revisa en el dominio (IDE-002, IDE-003, IDE-004). */
export function problemaDeFrase(frase: string) {
  if (frase.length < 15) {
    return problema(400, 'IDE-002', 'Frase de contraseña muy corta', {
      detail: `La frase debe tener al menos 15 caracteres. Tiene ${frase.length}.`,
    })
  }
  if (frase.length > 128) return problema(400, 'IDE-004', 'Frase de contraseña muy larga')
  if (FRASES_COMUNES.includes(frase.toLowerCase()) || new Set(frase).size === 1) {
    return problema(400, 'IDE-003', 'Frase de contraseña común')
  }
  return null
}

/** Reglas comunes de la cuenta: correo repetido y versión de la política (IDE-001, IDE-007). */
export function problemaDeCuenta(correo: string, versionPoliticaDatos: string) {
  if (versionPoliticaDatos !== VERSION_POLITICA_VIGENTE) {
    return problema(409, 'IDE-007', 'Versión de la política desactualizada', {
      detail: `La versión vigente es la ${VERSION_POLITICA_VIGENTE}.`,
    })
  }
  if (base.usuarios.some((u) => u.correo.toLowerCase() === correo.toLowerCase())) {
    return problema(409, 'IDE-001', 'Correo ya registrado')
  }
  return null
}

function perfil(usuario: UsuarioSimulado) {
  return {
    id: usuario.id,
    nombre: usuario.nombre,
    correo: usuario.correo,
    telefono: usuario.telefono,
    rol: usuario.rol,
  }
}

interface RegistroCliente {
  nombre: string
  correo: string
  telefono: string
  fraseContrasena: string
  aceptaPoliticaDatos: boolean
  versionPoliticaDatos: string
}

export const manejadoresIdentidad = [
  http.post(`${RUTA}/registro`, async ({ request }) => {
    const datos = await leerJson<RegistroCliente>(request)
    const invalidos = validar([
      ['nombre', vacio(datos.nombre) || (datos.nombre ?? '').length > 120, 'Escribe tu nombre (máximo 120 caracteres)'],
      ['correo', vacio(datos.correo) || !CORREO_VALIDO.test(datos.correo ?? ''), 'El correo no es válido'],
      [
        'telefono',
        !TELEFONO_VALIDO.test(datos.telefono ?? ''),
        'El teléfono debe tener entre 7 y 15 dígitos, con + opcional al inicio',
      ],
      ['fraseContrasena', vacio(datos.fraseContrasena), 'La frase de contraseña es obligatoria'],
      ['aceptaPoliticaDatos', datos.aceptaPoliticaDatos !== true, 'Debes aceptar la política de tratamiento de datos'],
      ['versionPoliticaDatos', vacio(datos.versionPoliticaDatos), 'La versión de la política aceptada es obligatoria'],
    ])
    if (invalidos) return invalidos

    const datosCompletos = datos as RegistroCliente
    const rechazo =
      problemaDeFrase(datosCompletos.fraseContrasena) ??
      problemaDeCuenta(datosCompletos.correo, datosCompletos.versionPoliticaDatos)
    if (rechazo) return rechazo

    const nuevo: UsuarioSimulado = {
      id: crypto.randomUUID(),
      nombre: datosCompletos.nombre,
      correo: datosCompletos.correo,
      telefono: datosCompletos.telefono,
      rol: 'CLIENTE',
      fraseContrasena: datosCompletos.fraseContrasena,
      estaSuspendido: false,
      fechaCreacion: new Date().toISOString(),
    }
    base.usuarios.push(nuevo)
    return HttpResponse.json(
      { id: nuevo.id, nombre: nuevo.nombre, correo: nuevo.correo, rol: nuevo.rol, fechaCreacion: nuevo.fechaCreacion },
      { status: 201, headers: { Location: '/api/v1/usuarios/me' } },
    )
  }),

  http.post(`${RUTA}/login`, async ({ request }) => {
    const { correo, fraseContrasena } = await leerJson<{ correo: string; fraseContrasena: string }>(request)
    const invalidos = validar([
      ['correo', vacio(correo), 'El correo es obligatorio'],
      ['fraseContrasena', vacio(fraseContrasena), 'La frase de contraseña es obligatoria'],
    ])
    if (invalidos) return invalidos

    const usuario = base.usuarios.find((u) => u.correo.toLowerCase() === correo?.toLowerCase())
    if (!usuario || usuario.fraseContrasena !== fraseContrasena) {
      return problema(401, 'IDE-008', 'Credenciales incorrectas')
    }
    if (usuario.estaSuspendido) return problema(403, 'IDE-009', 'Cuenta suspendida')

    if (usuario.rol === 'ADMIN') {
      const tokenSegundoFactor = crypto.randomUUID()
      base.segundosFactoresPendientes.set(tokenSegundoFactor, usuario.id)
      return HttpResponse.json({
        segundoFactorRequerido: true,
        tokenSegundoFactor,
        fechaExpiracion: new Date(Date.now() + CINCO_MINUTOS_MS).toISOString(),
      })
    }
    return respuestaSesion(usuario)
  }),

  http.post(`${RUTA}/totp`, async ({ request }) => {
    const { tokenSegundoFactor, codigo } = await leerJson<{ tokenSegundoFactor: string; codigo: string }>(request)
    const invalidos = validar([
      ['tokenSegundoFactor', vacio(tokenSegundoFactor), 'Falta el token del segundo factor'],
      ['codigo', !/^\d{6}$/.test(codigo ?? ''), 'El código tiene 6 dígitos'],
    ])
    if (invalidos) return invalidos

    const usuarioId = base.segundosFactoresPendientes.get(tokenSegundoFactor ?? '')
    const usuario = base.usuarios.find((u) => u.id === usuarioId)
    if (!usuario) return problema(401, 'IDE-013', 'Verificación vencida')
    if (codigo !== CODIGO_TOTP_DE_PRUEBA) return problema(401, 'IDE-012', 'Código incorrecto')

    base.segundosFactoresPendientes.delete(tokenSegundoFactor ?? '')
    return respuestaSesion(usuario)
  }),

  // Sin backend no hay cookie HttpOnly de refresco: al recargar la página hay que volver a entrar.
  http.post(`${RUTA}/refresco`, () => problema(401, 'IDE-010', 'Sesión vencida')),

  http.post(`${RUTA}/logout`, () => new HttpResponse(null, { status: 204 })),

  http.get('/api/v1/usuarios/me', ({ request }) => {
    const usuario = autorizar(request)
    if (usuario instanceof Response) return usuario
    return HttpResponse.json(perfil(usuario))
  }),

  http.patch('/api/v1/usuarios/me', async ({ request }) => {
    const usuario = autorizar(request)
    if (usuario instanceof Response) return usuario
    const { nombre, telefono } = await leerJson<{ nombre: string; telefono: string }>(request)
    const invalidos = validar([
      ['nombre', vacio(nombre) || (nombre ?? '').length > 120, 'El nombre es obligatorio (máximo 120 caracteres)'],
      [
        'telefono',
        !TELEFONO_VALIDO.test(telefono ?? ''),
        'El teléfono debe tener entre 7 y 15 dígitos, con + opcional al inicio',
      ],
    ])
    if (invalidos) return invalidos
    usuario.nombre = nombre as string
    usuario.telefono = telefono as string
    return HttpResponse.json(perfil(usuario))
  }),
]
