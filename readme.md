# Лабораторная работа №8

**Тема:** документация API (SpringDoc), контейнеризация (Docker) и CI/CD

---

## Описание проекта

REST API сервиса доставки еды — продолжение ЛР-4–7: CRUD-сущности, JWT-аутентификация, автотесты. В текущей работе добавлены интерактивная документация Swagger UI, Docker-образ приложения, полный стек через docker-compose (postgres + app + pgAdmin) и автоматическая публикация образа в GHCR при push в main.

Основные сущности:

| Сущность    | Назначение                                      |
|:------------|:------------------------------------------------|
| User        | Пользователь (email, пароль BCrypt, роль)       |
| Restaurant  | Ресторан                                        |
| Dish        | Блюдо, привязанное к ресторану                  |
| Order       | Заказ пользователя, содержащий список блюд      |

Документация API: `http://localhost:8080/swagger-ui.html`  
OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## Структура проекта

```
lab8/
├── src/main/kotlin/com/example/lab3/
│   ├── api/                    # REST-контроллеры с @Operation, @Tag
│   ├── config/                 # OpenApiConfig
│   ├── security/               # SecurityConfig (+ пути Swagger)
│   └── ...
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
├── Dockerfile                  # multi-stage: builder + JRE runtime
├── .dockerignore
├── docker-compose.yaml         # postgres + app + pgadmin
├── .env.example                # шаблон переменных окружения
├── .github/workflows/
│   ├── ci.yaml                 # тесты на pull_request
│   └── cd.yaml                 # сборка и push образа в GHCR
└── pom.xml                     # springdoc-openapi-starter-webmvc-ui
```

### Стек в docker-compose

```
postgres (healthcheck)
    ↓ service_healthy
app (Spring Boot)  ←→  pgadmin (веб-UI для БД)
```

---

## Реализованное в ЛР-8

### 1. SpringDoc OpenAPI

**Файл:** `pom.xml`

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.9</version>
</dependency>
```

**Файл:** `src/main/kotlin/com/example/lab3/config/OpenApiConfig.kt`

```kotlin
@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Food Delivery API")
                .version("1.0.0")
                .description("REST API сервиса доставки еды")
        )
}
```

**Файл:** `src/main/kotlin/com/example/lab3/security/SecurityConfig.kt` — пути Swagger открыты без JWT:

```kotlin
it.requestMatchers(
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**"
).permitAll()
```

**Аннотированные эндпoинты** (Auth, Restaurants, Orders):

```kotlin
@Tag(name = "Auth", description = "Регистрация и аутентификация")
@Operation(summary = "Регистрация нового пользователя")
@ApiResponses(
    value = [
        ApiResponse(responseCode = "201", description = "Пользователь создан, возвращён JWT"),
        ApiResponse(responseCode = "409", description = "Email уже занят")
    ]
)
```

Аналогичные аннотации — в `RestaurantController` (`GET /`, `GET /{id}`, `POST /`) и `OrderController` (`POST /`).

---

### 2. Dockerfile (multi-stage)

**Файл:** `Dockerfile`

```dockerfile
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Файл:** `.dockerignore` — исключает `target/`, `.git/`, `.env`, `*.md` и др.

---

### 3. docker-compose и переменные окружения

**Файл:** `docker-compose.yaml`

```yaml
postgres:
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
    interval: 5s
    timeout: 5s
    retries: 5

app:
  build: .
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}
    SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER}
    SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
    JWT_SECRET: ${JWT_SECRET}
  depends_on:
    postgres:
      condition: service_healthy

pgadmin:
  image: dpage/pgadmin4
  ports:
    - "5050:80"
```

**Файл:** `.env.example` — ключи без значений (для документации).  
**Файл:** `.env` — реальные значения, добавлен в `.gitignore`.

| Переменная         | Назначение                          |
|:-------------------|:------------------------------------|
| POSTGRES_DB        | имя базы данных                     |
| POSTGRES_USER      | пользователь PostgreSQL             |
| POSTGRES_PASSWORD  | пароль PostgreSQL                   |
| PGADMIN_EMAIL      | логин pgAdmin                       |
| PGADMIN_PASSWORD   | пароль pgAdmin                      |
| JWT_SECRET         | секрет для подписи JWT (≥ 32 симв.) |
| SERVER_PORT        | порт приложения на хосте            |

---

### 4. CD: публикация образа в GHCR

**Файл:** `.github/workflows/cd.yaml`

```yaml
on:
  push:
    branches: [main, master]

permissions:
  contents: read
  packages: write

jobs:
  build-and-push:
    steps:
      - uses: actions/checkout@v4
      - uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      - uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ghcr.io/${{ github.repository_owner }}/food-delivery:latest
```

Образ появляется в разделе **Packages** GitHub-профиля после push в main.

---

### 5. CI: триггер на pull_request

**Файл:** `.github/workflows/ci.yaml`

```yaml
on:
  pull_request:
    branches: [main, master]
```

Тесты запускаются только на PR (без `push` / `workflow_dispatch`).

---

## Запуск

### Весь стек через Docker Compose

```bash
# Скопировать шаблон и заполнить значения
cp .env.example .env

# Собрать образ и поднять postgres + app + pgadmin
docker compose up --build
```

После старта:

| Сервис      | URL                              |
|:------------|:---------------------------------|
| REST API    | http://localhost:8080            |
| Swagger UI  | http://localhost:8080/swagger-ui.html |
| pgAdmin     | http://localhost:5050            |

В pgAdmin для подключения к БД укажите хост `postgres` (имя сервиса в compose).

### Только образ приложения

```bash
docker build -t food-delivery:latest .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/delivery \
  -e JWT_SECRET=local-dev-jwt-secret-key-min-32-chars!! \
  food-delivery:latest
```

### Локальная разработка (без Docker для app)

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

### Автотесты

```bash
./mvnw test
```

---

## CI / CD

| Workflow   | Триггер              | Действие                          |
|:-----------|:---------------------|:----------------------------------|
| `ci.yaml`  | pull_request → main  | `./mvnw test`                     |
| `cd.yaml`  | push → main          | `docker build` + push в GHCR      |

---

## Скриншоты



---
## Эндпоинты (кратко)

| Метод  | Путь                    | Доступ              |
|:-------|:------------------------|:--------------------|
| POST   | `/auth/register`        | публичный           |
| POST   | `/auth/login`           | публичный           |
| GET    | `/api/v1/restaurants`   | публичный           |
| POST   | `/api/v1/restaurants`   | ADMIN               |
| POST   | `/api/v1/orders`        | USER / ADMIN + JWT  |
| GET    | `/swagger-ui.html`      | публичный           |
| GET    | `/v3/api-docs`          | публичный           |

Полное описание с примерами запросов — в Swagger UI.
