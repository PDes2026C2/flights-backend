## Stack completo con Docker Compose

El archivo Compose global está en [tusViajes-frontend](https://github.com/PDes2026C2/tusViajes-frontend#stack-completo-con-docker-compose). Levanta **PostgreSQL 16 → API de vuelos → Backend → Frontend**, esperando a que cada dependencia esté saludable. Sólo se necesitan Git y Docker con Compose v2.17 o superior y contenedores Linux; Java y Maven se ejecutan dentro de Docker.

Clonar el repositorio del frontend en una carpeta hermana de este repositorio. Desde la raíz del repositorio de la API de vuelos, ejecutar:

```bash
docker compose --project-directory ../tusViajes-frontend -f ../tusViajes-frontend/compose.yaml up -d --build --wait --wait-timeout 240
```

Las direcciones predeterminadas son http://localhost:5173 para el frontend y http://localhost:8081/flights para esta API. Los puertos, las credenciales y las imágenes se configuran en el archivo `.env` del frontend, usando su `.env.example` como referencia.

Las APIs utilizan las imágenes publicadas desde `main`; este comando no compila los cambios locales de las APIs. El Compose global crea su propia red y un volumen persistente de base de datos, compartido por ambas APIs. Consultar la [guía del frontend](https://github.com/PDes2026C2/tusViajes-frontend#stack-completo-con-docker-compose) para conocer los datos iniciales y los comandos de consulta de logs, actualización y apagado. La configuración local que se describe a continuación es una alternativa para desarrollar esta API.

## 🐳 Configuración del entorno de desarrollo local

Este proyecto utiliza una base de datos PostgreSQL para almacenar pasajeros, vuelos y sus relaciones.

Como esta base de datos también se comparte con el proyecto `tusViajes-webapp`, se utiliza un único contenedor PostgreSQL independiente y una red Docker personalizada para evitar conflictos de puertos.

### Paso 1: Crear la red compartida

Abrir una terminal y crear la red externa de Docker que utilizarán ambos proyectos:

```bash
docker network create local-dev-network
```

### Paso 2: Iniciar la base de datos compartida (omitir si ya está creada)

Fuera del directorio del proyecto, o en una carpeta dedicada a infraestructura, crear un archivo `docker-compose.yml` para la base de datos:

```yaml
services:
  postgres-shared:
    image: postgres:16-alpine
    container_name: postgres-shared
    environment:
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: password
      POSTGRES_DB: postgres
      # Cambiar el usuario y la contraseña según sea necesario.
    ports:
      - "5432:5432"
    volumes:
      - ./init-scripts:/docker-entrypoint-initdb.d
    networks:
      - local-dev-network

networks:
  local-dev-network:
    external: true
```

Iniciar la base de datos en segundo plano:

```bash
docker compose up -d
```

### Paso 3: Configurar el archivo .env

Antes de ejecutar la API, configurar las variables de entorno locales. Copiar el archivo `.env.example` a `.env` y completar las credenciales de la base de datos:

```bash
cp .env.example .env
```

### Paso 4: Ejecutar la API de vuelos

```bash
docker compose up --build
```

## Observabilidad: salud y métricas de Prometheus

Actuator utiliza un puerto de gestión separado: **9092** de forma predeterminada. El Compose global mantiene ese puerto dentro de la red Docker. Se puede cambiar mediante la variable de entorno `MANAGEMENT_SERVER_PORT`.

- `GET /actuator/health`: estado de salud de la aplicación, sin detalles internos de sus dependencias.
- `GET /actuator/health/liveness` y `/actuator/health/readiness`: controles de actividad y disponibilidad para la orquestación de contenedores.
- `GET /actuator/prometheus`: cantidad de peticiones HTTP, histogramas de tiempos de respuesta, métricas de la JVM y del pool de conexiones a la base de datos, etiquetadas con `application="flights"`.

Al ejecutar Java localmente, estos endpoints están disponibles en `http://localhost:9092`. Los endpoints de vuelos utilizan el puerto **8081**. El puerto de gestión debe mantenerse fuera del ingress y del proxy del frontend.

El Compose global y la configuración de Prometheus, Grafana y sus alertas se mantienen en el repositorio `tusViajes-frontend`.

Las pruebas de integración levantan la API con puertos aleatorios para la aplicación y la gestión, y una base PostgreSQL aislada mediante Testcontainers. Verifican el estado de salud, las métricas exportadas, la separación de puertos y la lista de endpoints de gestión expuestos. Docker debe estar en ejecución.

```bash
# Ejecutar sólo las pruebas de integración de observabilidad
./mvnw -Dtest=ObservabilityIntegrationTest test

# Ejecutar todas las pruebas y compilar la aplicación
./mvnw clean verify
```

En Windows, reemplazar `./mvnw` por `.\mvnw.cmd`.

## Observabilidad: logs y correlación de solicitudes

La aplicación escribe logs JSON en formato ECS en la salida estándar, con fecha, nivel, nombre del servicio (`flights`), logger y mensaje. No hace falta instalar otra biblioteca de logging. Por defecto, la impresión directa de SQL está desactivada para conservar la salida de la aplicación en formato estructurado.

Cada solicitud HTTP de negocio recibe un identificador `X-Request-ID`. Si el cliente envía un UUID canónico válido, se reutiliza normalizado a minúsculas; si falta o es inválido, se genera uno nuevo. El mismo identificador se devuelve en el encabezado de la respuesta y se agrega como `requestId` a los logs emitidos durante el procesamiento síncrono de la solicitud. El contexto se restaura al finalizar, incluso cuando ocurre una excepción.

El registro `Solicitud HTTP finalizada` agrega los siguientes campos:

| Campo | Contenido |
|---|---|
| `requestId` | Identificador de correlación de la solicitud. |
| `httpMethod` | Método HTTP. |
| `httpRoute` | Plantilla de la ruta del controlador; `UNMAPPED` si no se resolvió uno, por ejemplo ante un rechazo de seguridad. |
| `httpStatus` | Código de respuesta HTTP. |
| `durationMs` | Tiempo de procesamiento de esta API en milisegundos. |

Las respuestas 2xx y 3xx se registran con nivel `INFO`, las 4xx con `WARN` y las 5xx con `ERROR`. Si una excepción sale sin resolver de la cadena de filtros, se registra estado 500. El registro HTTP no incluye el cuerpo, los encabezados de autenticación ni los parámetros de consulta; utiliza plantillas de ruta para evitar registrar identificadores del recurso en la URL. Las rutas de Actuator quedan excluidas del registro de solicitudes.

Cuando el backend llama a esta API, se conserva su `X-Request-ID`, de modo que ambos servicios pueden localizarse en los logs mediante el mismo identificador.

Para comprobarlo con la aplicación levantada:

```bash
curl -i -H "X-Request-ID: 09c409d2-cf9b-4a33-a5f5-388dcf4de99e" "http://localhost:8081/flights?size=1"
```

En PowerShell, usar `curl.exe` en lugar de `curl`. La respuesta debe incluir ese ID y la consola debe mostrar el registro JSON correspondiente. Si se usa el Compose global, adaptar el puerto a su configuración y consultar los logs desde el repositorio `tusViajes-frontend`, conservando las opciones con las que se levantó el entorno.

Las pruebas de `ObservabilityIntegrationTest` comprueban el formato JSON, la correlación entre logs y respuestas, el tratamiento de IDs ausentes o inválidos, las respuestas de error y el aislamiento entre solicitudes concurrentes. Requieren Docker para levantar PostgreSQL con Testcontainers.
