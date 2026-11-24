import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: '.', workers: 1, timeout: 500_000, expect: { timeout: 10_000 },
  reporter: [['line']],
  use: { baseURL: 'http://localhost:4173', locale: 'es-CO', timezoneId: 'America/Bogota',
    geolocation: { latitude: 2.4448, longitude: -76.6147 }, permissions: ['geolocation'],
    trace: 'off', actionTimeout: 15_000, navigationTimeout: 20_000 },
})
