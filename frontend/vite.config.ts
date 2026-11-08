/// <reference types="vitest/config" />
import path from 'node:path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import { VitePWA } from 'vite-plugin-pwa'

// DECISIONES-FRONTEND: F-05 (origen único con proxy), F-13 (PWA), F-17 (pruebas)
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    VitePWA({
      registerType: 'prompt',
      includeAssets: ['icono.svg'],
      manifest: {
        name: '¿Quién lo tiene?',
        short_name: 'Quién lo tiene',
        description: 'Pregunta una vez y las tiendas cercanas te responden si tienen lo que buscas.',
        lang: 'es-CO',
        start_url: '/',
        display: 'standalone',
        background_color: '#ffffff',
        theme_color: '#1d4ed8',
        icons: [
          { src: 'icono-192.png', sizes: '192x192', type: 'image/png' },
          { src: 'icono-512.png', sizes: '512x512', type: 'image/png' },
          { src: 'icono-512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        ],
      },
      workbox: {
        // Solo archivos estáticos. Las respuestas de /api tienen datos personales (Ley 1581): nunca se guardan.
        globPatterns: ['**/*.{js,css,html,svg,png,webp,woff2}'],
        navigateFallback: '/index.html',
        navigateFallbackDenylist: [/^\/api\//],
        runtimeCaching: [],
      },
    }),
  ],
  resolve: {
    alias: { '@': path.resolve(import.meta.dirname, 'src') },
  },
  server: {
    port: 5173,
    // El navegador ve la PWA y la API en el mismo origen: la cookie SameSite=Strict del refresco funciona sin CORS
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: false },
    },
  },
  test: {
    environment: 'jsdom',
    globals: false,
    setupFiles: ['./src/pruebas/configuracion.ts'],
    css: true,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      // La meta del 70 % aplica a la lógica, no a los componentes visuales (DECISIONES-FRONTEND sección 12)
      include: ['src/services/**', 'src/hooks/**', 'src/features/**/esquemas.ts', 'src/features/**/use*.ts'],
      exclude: ['src/services/api-tipos.ts'],
      thresholds: { lines: 70 },
    },
  },
})
