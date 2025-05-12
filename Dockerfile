FROM eclipse-temurin:17-jdk-focal as builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Add wait-for script to handle service dependencies
COPY wait-for.sh /wait-for.sh
RUN chmod +x /wait-for.sh

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

EXPOSE 8080

# Use wait-for script to ensure dependencies are ready
ENTRYPOINT ["/bin/sh", "-c", "/wait-for.sh ${DATABASE_HOST}:${DATABASE_PORT} -- /wait-for.sh ${REDIS_HOST}:${REDIS_PORT} -- java ${JAVA_OPTS} -jar app.jar"]
