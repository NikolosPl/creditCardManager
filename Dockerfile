# --- ETAP 1: Budowanie Frontendu (Angular) ---
FROM node:24-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/creditCardManager/package*.json ./
RUN npm ci --ignore-scripts
COPY frontend/creditCardManager/ ./
# Budujemy Angulara do standardowego folderu produkcyjnego wewnątrz etapu frontend-build
RUN ./node_modules/.bin/ng build --output-path dist

# --- ETAP 2: Budowanie Backend-u (Spring Boot) ---
FROM gradle:8.12-jdk21 AS backend-build
WORKDIR /app
COPY . .
# Kopiujemy zawartość zbudowanego dystrybucyjnego folderu Angulara bezpośrednio do zasobów statycznych Spring Boota
COPY --from=frontend-build /app/frontend/dist/ ./src/main/resources/static/
ENV IN_DOCKER=true
# Budujemy aplikację z pominięciem testów
RUN ./gradlew bootJar -x test

# --- ETAP 3: Uruchomienie gotowej aplikacji ---
FROM eclipse-temurin:26-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]