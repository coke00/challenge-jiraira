# Tenpo Challenge - API REST Spring Boot

## Descripción del Proyecto

Esta es una API REST desarrollada en Spring Boot con Java 21 que implementa las siguientes funcionalidades principales:

1. **Cálculo con porcentaje dinámico**: Endpoint que suma dos números y aplica un porcentaje obtenido de un servicio externo
2. **Caché del porcentaje**: Almacenamiento en Redis válido por 30 minutos con fallback en caso de fallo, ya sea para obtener el porcentaje anterior
3. **Reintentos**: Lógica de retry con máximo 3 intentos ante fallos del servicio externo, actualmente agregados como dato fijo pero podria ser enviado como variable de entorno configurable
4. **Historial de llamadas**: Registro asíncrono de todas las llamadas con consulta paginada
5. **Rate Limiting**: Máximo 3 RPM (requests por minuto)
6. **Manejo de errores**: Respuestas HTTP adecuadas para errores 4XX y 5XX

## Arquitectura y Decisiones Técnicas

### Stack Tecnológico
- **Java 21**: Última versión LTS con mejoras de rendimiento
- **Spring Boot 3.2.0**: Framework principal con auto-configuración
- **PostgreSQL**: Base de datos relacional para persistencia del historial
- **Redis**: Cache distribuido para el porcentaje del servicio externo
- **Bucket4j**: Implementación de rate limiting eficiente
- **Docker**: Containerización para despliegue escalable
- **Swagger/OpenAPI**: Documentación automática de la API

### Decisiones de Diseño

1. **Programación Bloqueante**: Se usa Spring MVC para llamadas al servicio externo, con manejo de timeouts y recursos de forma tradicional
2. **Cache Distribuido**: Redis permite escalabilidad horizontal manteniendo el cache compartido
3. **Logging Asíncrono**: El registro del historial no afecta el tiempo de respuesta usando `@Async`
4. **Rate Limiting por IP**: Implementación basada en la IP del cliente para controlar el uso
5. **Retry Pattern**: Configuración declarativa con `@Retryable` para manejo robusto de fallos
## Instrucciones de Despliegue

### Prerrequisitos
- Docker y Docker Compose instalados
- Java 21 (solo para desarrollo local)
- Gradle (incluido wrapper)

### Opción 1: Despliegue con Docker Compose (Recomendado)

```bash
# Clonar el repositorio
git clone <repository-url>
cd challenge-jiraira

# Construir y levantar todos los servicios
docker-compose up --build

# En segundo plano
docker-compose up -d --build
```

Los servicios estarán disponibles en:
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379

### Opción 2: Desarrollo Local

```bash
# Levantar solo las dependencias (PostgreSQL y Redis)
docker-compose up postgres redis -d

# Ejecutar la aplicación localmente
./gradlew bootRun

# O construir y ejecutar JAR
./gradlew build
java -jar build/libs/challenge-jiraira-1.0-SNAPSHOT.jar
```

### Opción 3: Solo con Docker

```bash
# Construir la imagen
./gradlew build
docker build -t challenge-jiraira .

# Ejecutar con variables de entorno
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/tenpo_db \
  challenge-jiraira
```

## Interacción con la API

### Endpoints Principales

#### 1. Cálculo con Porcentaje
```bash
POST /api/v1/calculations/calculate
Content-Type: application/json

{
  "num1": 5.0,
  "num2": 5.0
}
```

**Respuesta:**
```json
{
  "result": 11.0,
  "percentage": 10.0,
  "calculationDetails": "(5.0 + 5.0) + 10.0% = 10.0 + 1.0 = 11.0"
}
```

#### 2. Historial de Llamadas
```bash
GET /api/v1/history?page=0&size=10
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "timestamp": "2025-08-02T10:30:00",
    "endpoint": "/api/v1/calculations/calculate",
    "method": "POST",
    "parameters": "{\"num1\":5.0,\"num2\":5.0}",
    "response": "{\"result\":11.0,\"percentage\":10.0,\"details\":\"(5.0 + 5.0) + 10.0% = 10.0 + 1.0 = 11.0\"}",
    "statusCode": 200,
    "executionTimeMs": 150
  }
]
```

### Ejemplos con cURL

```bash
# Cálculo básico
curl -X POST http://localhost:8080/api/v1/calculations/calculate \
  -H "Content-Type: application/json" \
  -d '{"num1": 10, "num2": 15}'

# Consultar historial (página 0, tamaño 5)
curl "http://localhost:8080/api/v1/history?page=0&size=5"

# Health check
curl http://localhost:8080/actuator/health
```

## Testing

### Ejecutar Tests
```bash
# Todos los tests
./gradlew test

# Tests específicos
./gradlew test --tests "CalculationServiceTest"

# Tests de integración
./gradlew integrationTest
```

### Cobertura de Tests
Los tests cubren:
- Lógica de cálculo con diferentes escenarios
- Manejo de fallos del servicio externo
- Rate limiting (simulación de múltiples requests)
- Validación de parámetros
- Casos de error (cache vacío, servicio no disponible)

## Documentación de la API

La documentación completa está disponible en:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## Monitoreo

### Actuator Endpoints
- `/actuator/health`: Estado de la aplicación y dependencias
- `/actuator/metrics`: Métricas de la aplicación
- `/actuator/info`: Información de la aplicación

### Logs
Los logs están configurados para mostrar:
- Llamadas al servicio externo
- Uso del cache (hits/misses)
- Rate limiting aplicado
- Errores y excepciones

## Escalabilidad

### Características para Múltiples Réplicas
1. **Cache Distribuido**: Redis permite compartir cache entre instancias
2. **Base de Datos Compartida**: PostgreSQL con connection pooling
3. **Stateless**: La aplicación no mantiene estado en memoria
4. **Rate Limiting Distribuido**: Bucket4j con backend distribuido
5. **Health Checks**: Configurados para load balancers

### Configuración para Producción
```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      connection-timeout: 30000
  data:
    redis:
      timeout: 1000ms
      lettuce:
        pool:
          max-active: 10
```

## Consideraciones de Seguridad

1. **Usuario no root**: Dockerfile configura usuario específico
2. **Validación de entrada**: Validación completa de parámetros
3. **Rate Limiting**: Protección contra abuso
4. **Error Handling**: No exposición de información sensible
5. **Health Checks**: Monitoreo de dependencias

## Mejoras Futuras

1. **Autenticación**: JWT o OAuth2 para securing endpoints
2. **Métricas Avanzadas**: Micrometer con Prometheus
3. **Distributed Tracing**: Sleuth + Zipkin
4. **Circuit Breaker**: Resilience4j para el servicio externo
5. **API Versioning**: Estrategia de versionado para evolución

## Docker Hub

La imagen está disponible en Docker Hub: `[usuario]/challenge-jiraira:latest`

```bash
docker pull [usuario]/challenge-jiraira:latest
docker run -p 8080:8080 [usuario]/challenge-jiraira:latest
```

## Contacto

Para cualquier consulta sobre la implementación, por favor contactar al desarrollador.
