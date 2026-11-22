import { defineConfig } from '@playwright/test'

/**
 * Escenarios de punta a punta E2E-01 a E2E-05 (docs/pruebas/libro/02-CASOS.md, plan del C3 §3).
 * Corren contra la PWA y la API desplegadas juntas: por defecto la pila local (vite preview con proxy a la API en
 * 8080); con E2E_URL contra otro entorno. Ver e2e/README.md.
 */
export default defineConfig({
  testDir: './pruebas',
  // Los escenarios comparten la pila y la tienda abierta de un escenario recibe solicitudes de otro: uno a la vez
  workers: 1,
  fullyParallel: false,
  timeout: 120_000,
  expect: { timeout: 15_000 },
  reporter: [['list'], ['html', { open: 'never', outputFolder: 'reporte' }], ['json', { outputFile: 'reporte/resultados.json' }]],
  use: {
    baseURL: process.env.E2E_URL ?? 'http://localhost:4173',
    locale: 'es-CO',
    timezoneId: 'America/Bogota',
    // Popayán: la tienda y el cliente quedan cerca (radio inicial de 3 km)
    geolocation: { latitude: 2.4448, longitude: -76.6147 },
    permissions: ['geolocation'],
    trace: 'off',
    screenshot: 'only-on-failure',
    video: 'off',
  },
})
