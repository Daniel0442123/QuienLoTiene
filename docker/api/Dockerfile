# syntax=docker/dockerfile:1
# Imagen de la API (diagrama 08-despliegue). Se construye desde la raíz del repo:
#   docker build -f docker/api/Dockerfile -t quienlotiene-api .

# --- Compilación: el jar de Spring Boot separado en capas para que las dependencias queden en caché ---
FROM eclipse-temurin:21-jdk AS compilacion
WORKDIR /fuente
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# El wrapper descarga Maven con wget sin reintentos y a veces falla en los runners de GitHub: se reintenta dos veces
RUN --mount=type=cache,target=/root/.m2 for intento in 1 2 3; do \
        ./mvnw -B -q dependency:go-offline && exit 0; \
        echo "Descarga de Maven o dependencias falló (intento $intento), se reintenta en 15 s"; sleep 15; \
    done; exit 1
COPY src/ src/
# Las pruebas ya corren en el workflow de despliegue antes de construir la imagen
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q -DskipTests -Dspotless.check.skip=true package \
    && cp target/*.jar quienlotiene.jar \
    && java -Djarmode=tools -jar quienlotiene.jar extract --layers --destination /extraido

# --- Ejecución: solo el JRE, con un usuario sin privilegios ---
FROM eclipse-temurin:21-jre
# La base Ubuntu 26.04 trae pebble (gestor de servicios en Go) con CVE-2026-97054 en su stdlib; la API no lo usa
RUN rm -f /usr/bin/pebble \
    && groupadd --system quienlotiene \
    && useradd --system --gid quienlotiene --home-dir /app --shell /usr/sbin/nologin quienlotiene \
    && mkdir -p /datos/archivos \
    && chown quienlotiene:quienlotiene /datos/archivos
WORKDIR /app
COPY --from=compilacion /extraido/dependencies/ ./
COPY --from=compilacion /extraido/spring-boot-loader/ ./
COPY --from=compilacion /extraido/snapshot-dependencies/ ./
COPY --from=compilacion /extraido/application/ ./
USER quienlotiene
EXPOSE 8080
# El contenedor tiene 640 MB (compose.prod.yaml): 60 % para el heap y el resto para metaspace, hilos y buffers
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+ExitOnOutOfMemoryError" \
    QLT_ARCHIVOS_DIRECTORIO=/datos/archivos
ENTRYPOINT ["java", "-jar", "quienlotiene.jar"]
