# K-and-L-HEALTH | Sistema de Gestión Clínica (Backend)

Un sistema de gestión clínica escalable y resiliente construido con una arquitectura orientada a microservicios. Este proyecto gestiona el ciclo de vida completo de citas médicas, historiales de pacientes, facturación y catálogos de usuarios (doctores y pacientes).

> ⚠️ **Estado del Proyecto:** En desarrollo activo. Actualmente completando la configuración de resiliencia de red y la suite de pruebas unitarias/integración para el microservicio de citas (`mcsv-citas`).

## 🛠️ Stack Tecnológico

- **Lenguaje:** Java 21 (Aprovechando Virtual Threads para alta concurrencia)
- **Framework Core:** Spring Boot 4.1.1
- **Arquitectura y Enrutamiento:** Spring Cloud (Netflix Eureka, API Gateway, Config Server)
- **Comunicación y Resiliencia:** OpenFeign, Resilience4j (Circuit Breaker con interceptación AOP)
- **Persistencia de Datos:** Spring Data JPA, PostgreSQL
- **Control de Versiones de BD:** Flyway
- **Mapeo y Utilidades:** MapStruct, Lombok
- **Testing:** JUnit 5, Mockito
- **Documentación:** Springdoc OpenAPI (Swagger UI)

## 🏗️ Arquitectura del Sistema

El proyecto sigue los principios de **Clean Architecture** y **Domain-Driven Design (DDD)** dentro de cada microservicio, asegurando un bajo acoplamiento entre la lógica de negocio y las dependencias de infraestructura.

### Ecosistema de Microservicios
1. **`microservice-config`**: Servidor centralizado de configuraciones.
2. **`microservice-eureka`**: Service Discovery para registro y localización dinámica de instancias.
3. **`microservice-gateway`**: Punto de entrada único, balanceo de carga y enrutamiento.
4. **`microservice-users`**: Gestión del dominio de Pacientes y Doctores.
5. **`microservice-citas`**: Core del agendamiento. Maneja solapamientos de horarios (ZonedDateTime), validaciones de anticipación y estados de citas.
6. **`microservice-historial-clinico`**: *(En planificación)*
7. **`microservice-factura`**: *(En planificación)*

## ⚙️ Características Técnicas Destacadas

* **Manejo Centralizado de Excepciones:** Implementación de un `@RestControllerAdvice` global que estandariza las respuestas de error (reglas de negocio, validaciones de DTOs, fallos de formato JSON) evitando exponer el stacktrace al cliente.
* **Tolerancia a Fallos (Circuit Breaker):** Configuración de *fallback methods* mediante adaptadores (`UserClientAdapter`) para gestionar caídas de red o indisponibilidad temporal entre `mcsv-citas` y `mcsv-users`, devolviendo códigos HTTP `503 Service Unavailable` controlados.
* **Prevención de Falsos Positivos en Resiliencia:** Configuración de omisión de excepciones para respuestas `404 Not Found` en llamadas Feign, evitando que consultas legítimas de recursos inexistentes abran el circuito de Resilience4j.
* **Validación de Reglas de Negocio:** Uso de polimorfismo y listas de validadores (`List<ValidarCreacionCita>`) inyectados dinámicamente por Spring para aplicar reglas complejas de forma limpia y extensible.

## 🚀 Instalación y Ejecución Local

### Requisitos Previos
* JDK 21+
* Maven 3.8+
* Docker y Docker Compose (Para levantar instancias de PostgreSQL)

