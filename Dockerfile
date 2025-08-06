FROM eclipse-temurin:21-jre

LABEL maintainer="Challenge-Tenpo"
LABEL description="API Spring Boot para challenge técnico"


RUN groupadd -r tenpo && useradd -r -g tenpo tenpo
WORKDIR /app
COPY build/libs/*.jar app.jar
RUN chown tenpo:tenpo app.jar
USER tenpo

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=docker
