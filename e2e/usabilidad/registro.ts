import fs from 'node:fs'
import type { Page } from '@playwright/test'
export const DIR = process.env.UP_DIR ?? '../docs/pruebas/usabilidad/evidencias'
fs.mkdirSync(`${DIR}/capturas`, { recursive: true })
export const filas: any[] = []
export async function avisos(p: Page): Promise<string> {
  const textos = await p.locator('[role=alert], [role=status], [aria-live]:not(:empty), [id$="-error"], .text-destructive').allInnerTexts().catch(() => [])
  return [...new Set(textos.map((t) => t.replace(/\s+/g, ' ').trim()).filter((t) => t.length > 1))].join(' | ').slice(0, 400)
}
export async function caso(id: string, p: Page, fn: () => Promise<{ ok: boolean; nota?: string; inicio?: number; toques?: number }>) {
  const t0 = Date.now()
  let r: any
  try { r = await fn() } catch (e: any) { r = { ok: false, nota: 'Error: ' + String(e.message ?? e).split('\n')[0].slice(0, 200) } }
  const captura = `capturas/${id}.png`
  await p.screenshot({ path: `${DIR}/${captura}`, fullPage: false }).catch(() => {})
  const fila = { id, resultado: r.ok ? 'Pasa' : 'No pasa', segundos: Math.round(((Date.now() - (r.inicio ?? t0)) / 1000) * 10) / 10, toques: r.toques ?? null, observado: r.nota ?? (await avisos(p)), captura }
  filas.push(fila); console.log(JSON.stringify(fila))
  fs.writeFileSync(`${DIR}/resultados.json`, JSON.stringify(filas, null, 2))
}
