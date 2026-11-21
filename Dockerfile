# Etapa 1: compila y ejecuta las pruebas con JDK 17 y Maven 3.9
FROM maven:3.9-eclipse-temurin-17 AS construccion
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B clean package

# Etapa 2: imagen de ejecucion, solo con el JRE y el JAR
# Sin instruccion VOLUME: Railway la rechaza y monta su propio volumen en /data.
FROM eclipse-temurin:17-jre
RUN useradd --system --create-home comercio \
    && mkdir /data && chown comercio:comercio /data
WORKDIR /home/comercio
COPY --from=construccion /app/target/comerciocontrol-api.jar app.jar
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN sed -i 's/\r$//' /usr/local/bin/docker-entrypoint.sh \
    && chmod 755 /usr/local/bin/docker-entrypoint.sh
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
HEALTHCHECK --interval=20s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -fsS http://localhost:8080/api/health || exit 1
# El script entrega /data al usuario comercio y ejecuta la API sin privilegios.
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
