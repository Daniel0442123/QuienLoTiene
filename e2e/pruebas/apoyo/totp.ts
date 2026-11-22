import { createHmac } from 'node:crypto'

/** Código TOTP de 6 dígitos (RFC 6238, SHA-1, 30 s), el mismo que muestra la aplicación autenticadora. */
export function codigoTotp(secretoBase32: string, ahora = Date.now()): string {
  const alfabeto = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ234567'
  let bits = ''
  for (const letra of secretoBase32.replace(/=+$/, '').toUpperCase()) {
    bits += alfabeto.indexOf(letra).toString(2).padStart(5, '0')
  }
  const llave = Buffer.from(bits.match(/.{8}/g)!.map((b) => parseInt(b, 2)))
  const contador = Buffer.alloc(8)
  contador.writeBigUInt64BE(BigInt(Math.floor(ahora / 1000 / 30)))
  const hmac = createHmac('sha1', llave).update(contador).digest()
  const desfase = hmac[hmac.length - 1] & 0x0f
  const valor = (hmac.readUInt32BE(desfase) & 0x7fffffff) % 1_000_000
  return valor.toString().padStart(6, '0')
}

/** Espera al inicio de una ventana nueva si al código le quedan menos de 5 s, para que no cambie al enviarlo. */
export async function codigoTotpSeguro(secretoBase32: string): Promise<string> {
  const restante = 30_000 - (Date.now() % 30_000)
  if (restante < 5_000) await new Promise((r) => setTimeout(r, restante + 200))
  return codigoTotp(secretoBase32)
}
