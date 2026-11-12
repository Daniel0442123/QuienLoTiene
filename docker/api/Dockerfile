# syntax=docker/dockerfile:1
# Imagen de la API (diagrama 08-despliegue). Se construye desde la raíz del repo:
#   docker build -f docker/api/Dockerfile -t quienlotiene-api .

# --- Compilación: el jar de Spring Boot separado en capas para que las dependencias queden en caché ---
FROM eclipse-temurin:21-jdk AS compilacion
WORKDIR /fuente
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q dependency:go-offline
COPY src/ src/
# Las pruebas ya corren en el workflow de despliegue antes de construir la imagen
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q -DskipTests -Dspotless.check.skip=true package \
    && cp target/*.jar quienlotiene.jar \
    && java -Djarmode=tools -jar quienlotiene.jar extract --layers --destination /extraido

# --- Ejecución: solo el JRE, con un usuario sin privilegios ---
FROM eclipse-temurin:21-jre
RUN groupadd --system quienlotiene \
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
