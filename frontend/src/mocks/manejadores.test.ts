import { describe, expect, it } from 'vitest'
import { CATEGORIAS_SIMULADAS, CODIGO_TOTP_DE_PRUEBA, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from './datos'

/** La simulación de la API responde como el contrato: si se desvía, las pantallas probadas con ella mienten. */

async function publicar(ruta: string, cuerpo: unknown, token?: string) {
  return fetch(ruta, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: JSON.stringify(cuerpo),
  })
}

async function entrar(correo: string): Promise<string> {
  const respuesta = await publicar('/api/v1/auth/login', { correo, fraseContrasena: FRASE_DE_PRUEBA })
  const datos = (await respuesta.json()) as { tokenAcceso: string }
  return datos.tokenAcceso
}

const solicitudValida = {
  producto: 'Acetaminofén 500 mg',
  cantidad: 1,
  categoriaId: CATEGORIAS_SIMULADAS[0].id,
  modalidad: 'RECOGER',
  ubicacion: { latitud: 2.44, longitud: -76.6 },
}

describe('Simulación de la API · identidad', () => {
  it('entrega la sesión del cliente y de la tienda con su rol', async () => {
    // Dado / Cuando
    const cliente = await publicar('/api/v1/auth/login', {
      correo: CORREOS_DE_PRUEBA.cliente,
      fraseContrasena: FRASE_DE_PRUEBA,
    })
    const tienda = await publicar('/api/v1/auth/login', {
      correo: CORREOS_DE_PRUEBA.tienda,
      fraseContrasena: FRASE_DE_PRUEBA,
    })

    // Entonces
    expect(await cliente.json()).toMatchObject({ rol: 'CLIENTE', tipoToken: 'Bearer', segundoFactorRequerido: false })
    expect(await tienda.json()).toMatchObject({ rol: 'TIENDA' })
  })

  it('pide el segundo factor al administrador y lo acepta con el código de prueba', async () => {
    // Dado
    const inicio = await publicar('/api/v1/auth/login', {
      correo: CORREOS_DE_PRUEBA.administrador,
      fraseContrasena: FRASE_DE_PRUEBA,
    })
    const pendiente = (await inicio.json()) as { segundoFactorRequerido: boolean; tokenSegundoFactor: string }
    expect(pendiente.segundoFactorRequerido).toBe(true)

    // Cuando
    const incorrecto = await publicar('/api/v1/auth/totp', {
      tokenSegundoFactor: pendiente.tokenSegundoFactor,
      codigo: '000000',
    })
    const correcto = await publicar('/api/v1/auth/totp', {
      tokenSegundoFactor: pendiente.tokenSegundoFactor,
      codigo: CODIGO_TOTP_DE_PRUEBA,
    })

    // Entonces
    expect(await incorrecto.json()).toMatchObject({ status: 401, codigo: 'IDE-012' })
    expect(await correcto.json()).toMatchObject({ rol: 'ADMIN' })
  })

  it('responde IDE-009 a la cuenta suspendida y IDE-001 al correo repetido', async () => {
    // Dado / Cuando
    const suspendida = await publicar('/api/v1/auth/login', {
      correo: CORREOS_DE_PRUEBA.suspendido,
      fraseContrasena: FRASE_DE_PRUEBA,
    })
    const repetido = await publicar('/api/v1/auth/registro', {
      nombre: 'Otra persona',
      correo: CORREOS_DE_PRUEBA.cliente,
      telefono: '3001234567',
      fraseContrasena: 'otra frase larga y segura',
      aceptaPoliticaDatos: true,
      versionPoliticaDatos: '1.0',
    })

    // Entonces
    expect(await suspendida.json()).toMatchObject({ status: 403, codigo: 'IDE-009' })
    expect(await repetido.json()).toMatchObject({ status: 409, codigo: 'IDE-001' })
  })

  it('marca el campo de la política cuando no se acepta (GEN-001 con errores)', async () => {
    // Cuando
    const respuesta = await publicar('/api/v1/auth/registro', {
      nombre: 'Persona',
      correo: 'nueva@ejemplo.com',
      telefono: '3001234567',
      fraseContrasena: 'otra frase larga y segura',
      aceptaPoliticaDatos: false,
      versionPoliticaDatos: '1.0',
    })

    // Entonces
    const cuerpo = (await respuesta.json()) as { codigo: string; errores: Array<{ campo: string }> }
    expect(cuerpo.codigo).toBe('GEN-001')
    expect(cuerpo.errores.map((e) => e.campo)).toEqual(['aceptaPoliticaDatos'])
  })
})

describe('Simulación de la API · autorización', () => {
  it('responde GEN-003 sin token y GEN-004 con el rol equivocado', async () => {
    // Dado
    const tokenTienda = await entrar(CORREOS_DE_PRUEBA.tienda)

    // Cuando
    const sinToken = await fetch('/api/v1/clientes/me/direcciones')
    const otroRol = await publicar('/api/v1/solicitudes', solicitudValida, tokenTienda)

    // Entonces
    expect(sinToken.status).toBe(401)
    expect(await sinToken.json()).toMatchObject({ codigo: 'GEN-003' })
    expect(await otroRol.json()).toMatchObject({ status: 403, codigo: 'GEN-004' })
  })
})

describe('Simulación de la API · solicitudes', () => {
  it('publica hasta 10 por hora y luego responde SOL-001 con Retry-After', async () => {
    // Dado
    const token = await entrar(CORREOS_DE_PRUEBA.cliente)
    for (let i = 0; i < 10; i++) {
      const respuesta = await publicar('/api/v1/solicitudes', solicitudValida, token)
      expect(respuesta.status).toBe(201)
    }

    // Cuando
    const undecima = await publicar('/api/v1/solicitudes', solicitudValida, token)

    // Entonces
    expect(undecima.status).toBe(429)
    expect(Number(undecima.headers.get('Retry-After'))).toBeGreaterThan(0)
    expect(await undecima.json()).toMatchObject({ codigo: 'SOL-001' })
  })
})

describe('Simulación de la API · tiendas y administración', () => {
  it('la tienda pendiente no puede abrir (TIE-008) hasta que el administrador la aprueba', async () => {
    // Dado
    const tokenTienda = await entrar(CORREOS_DE_PRUEBA.tiendaPendiente)
    const abrir = () =>
      fetch('/api/v1/tiendas/me/apertura', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${tokenTienda}` },
        body: JSON.stringify({ abierta: true }),
      })
    expect(await (await abrir()).json()).toMatchObject({ status: 409, codigo: 'TIE-008' })

    // Cuando
    const inicio = await publicar('/api/v1/auth/login', {
      correo: CORREOS_DE_PRUEBA.administrador,
      fraseContrasena: FRASE_DE_PRUEBA,
    })
    const { tokenSegundoFactor } = (await inicio.json()) as { tokenSegundoFactor: string }
    const sesionAdmin = await publicar('/api/v1/auth/totp', { tokenSegundoFactor, codigo: CODIGO_TOTP_DE_PRUEBA })
    const { tokenAcceso } = (await sesionAdmin.json()) as { tokenAcceso: string }
    const pendientes = (await (
      await fetch('/api/v1/admin/tiendas', { headers: { Authorization: `Bearer ${tokenAcceso}` } })
    ).json()) as Array<{ id: string }>
    const aprobacion = await publicar(`/api/v1/admin/tiendas/${pendientes[0]?.id}/aprobacion`, {}, tokenAcceso)

    // Entonces
    expect(await aprobacion.json()).toMatchObject({ estado: 'APROBADA' })
    expect(await (await abrir()).json()).toMatchObject({ estado: 'APROBADA', estaAbierta: true })
  })
})
