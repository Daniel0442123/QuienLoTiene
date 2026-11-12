# syntax=docker/dockerfile:1
# Caddy con el build de la PWA adentro (DECISIONES-FRONTEND F-05 y sección 13). Se construye desde la raíz del repo:
#   docker build -f docker/caddy/Dockerfile -t quienlotiene-caddy .

# --- Build del frontend con la misma versión de Node que CI ---
FROM node:24-alpine AS frontend
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci
COPY frontend/ ./
RUN npm run build

# --- Caddy: TLS de Let's Encrypt, la PWA en / y la API en /api ---
FROM caddy:2.10-alpine
COPY docker/caddy/Caddyfile /etc/caddy/Caddyfile
COPY --from=frontend /frontend/dist /srv
