FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd -r kasku && useradd -r -g kasku kasku && mkdir -p /app/data /app/.runtime && chown -R kasku:kasku /app
COPY --from=build --chown=kasku:kasku /build/target/classes ./target/classes
COPY --from=build --chown=kasku:kasku /build/target/dependency ./target/dependency
COPY --chown=kasku:kasku src/main/webapp ./src/main/webapp
USER kasku
ENV PORT=8080 JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
CMD ["java", "-cp", "target/classes:target/dependency/*", "id.kasku.Main"]
