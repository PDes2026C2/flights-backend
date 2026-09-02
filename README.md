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
      # Change admin and password as you need
    ports:
      - "5432:5432"
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

### Step 3: Run the Flights API

Once the database is up and running, navigate to the root of the flights project directory and start the application:

```bash
docker compose up --build
```
