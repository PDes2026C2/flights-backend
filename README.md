## Full stack with Docker Compose

The global Compose file lives in [tusViajes-frontend](https://github.com/PDes2026C2/tusViajes-frontend#stack-completo-con-docker-compose). It starts **PostgreSQL 16 → Flights API → Backend → Frontend**, waiting for each dependency to become healthy. Only Git and Docker with Compose v2.17 or newer and Linux containers are required; Java and Maven do not need to be installed locally.

Clone the frontend repository next to this repository. From the root of the Flights API repository, run:

```bash
docker compose --project-directory ../tusViajes-frontend -f ../tusViajes-frontend/compose.yaml up -d --build --wait --wait-timeout 240
```

The default URLs are http://localhost:5173 for the frontend and http://localhost:8081/flights for this API. Configure ports, credentials and images in the frontend's `.env`, using its `.env.example` as a reference.

The APIs use published `main` images; this command does not build local API changes. The global Compose creates its own network and persistent database volume, shared by both APIs. See the [frontend guide](https://github.com/PDes2026C2/tusViajes-frontend#stack-completo-con-docker-compose) for initial data, logs, updates and shutdown commands. The local setup below is a separate option for developing this API.

## 🐳 Local Development Setup

This project uses a PostgreSQL database to persist Passengers, Flights, and their relations. 

Since this database is also shared with the `tusViajes-webapp` project, we use a single, detached PostgreSQL container communicating through a custom Docker network to prevent port conflicts.

### Step 1: Create the Shared Network
First, open your terminal and create the external Docker network that both projects will use:

```bash
docker network create local-dev-network
```

### Step 2: Start the shared Database (Ignore if you have done that previously)
Outside of your project directory (or in a dedicated infrastructure folder), create a `docker-compose.yml` file for the database:
And create a compose for the database

```yaml
services:
  postgres-shared:
    image: postgres:16-alpine
    container_name: postgres-shared
    environment:
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: password
      POSTGRES_DB: postgres
      # Change admin and password as you need
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

Run the database in background: 

```bash
docker compose up -d
```

### Step 3: Set up .env

Before running the API, you need to set up your local environment variables. Duplicate the `.env.example` file, rename it to `.env`, and fill in your database credentials:

```bash
cp .env.example .env
```

### Step 4: Run the Flights API
```bash
docker compose up --build
```
