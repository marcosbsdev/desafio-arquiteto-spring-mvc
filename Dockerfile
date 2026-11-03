# ============================================================================
# Etapa 1: Build da aplicação com Maven e JDK 17
# ============================================================================
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copia o pom.xml e baixa as dependências primeiro para aproveitar cache de camadas
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte (incluindo migrations em src/main/resources/db/migration) e gera o JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================================
# Etapa 2: Imagem final enxuta de execução (Runtime JRE 17)
# ============================================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copia apenas o artefato compilado da etapa de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
