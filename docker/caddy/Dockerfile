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

# --- Binario de Caddy compilado con dependencias al día ---
# La imagen oficial trae golang.org/x/net con una vulnerabilidad alta ya corregida (CVE-2026-78661) y Trivy bloquea
# el despliegue (RNF-011). Al subir de versión de Caddy, quitar el --replace si la imagen oficial ya trae la corrección.
FROM caddy:2.11-builder-alpine AS caddy
RUN xcaddy build v2.11.9 --replace golang.org/x/net=golang.org/x/net@v0.62.0 --output /usr/bin/caddy

# --- Caddy: TLS de Let's Encrypt, la PWA en / y la API en /api ---
FROM caddy:2.11-alpine
COPY --from=caddy /usr/bin/caddy /usr/bin/caddy
COPY docker/caddy/Caddyfile /etc/caddy/Caddyfile
COPY --from=frontend /frontend/dist /srv
