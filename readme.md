# Лабораторная работа №9

**Тема:** кэширование (Redis, Spring Cache)

---

## Описание проекта

REST API сервиса доставки еды — продолжение ЛР-4–8: JWT, Swagger, Docker, CI/CD. В текущей работе добавлен слой кэширования на Redis: горячие read-запросы (список ресторанов, ресторан по id, меню) отдаются из кэша, при изменении данных кэш инвалидируется.

Основные сущности:

| Сущность    | Назначение                                      |
|:------------|:------------------------------------------------|
| User        | Пользователь (email, пароль BCrypt, роль)       |
| Restaurant  | Ресторан                                        |
| Dish        | Блюдо, привязанное к ресторану                  |
| Order       | Заказ пользователя, содержащий список блюд      |

Кэши: `restaurants` (TTL 1 ч), `dishes` (TTL 1 ч).

---

## Структура проекта

```
lab9/
├── src/main/kotlin/com/example/lab3/
│   ├── config/                 # CacheConfig, OpenApiConfig
│   ├── application/            # RestaurantService, DishService (+ @Cacheable)
│   └── ...
├── docker-compose.yaml         # postgres + redis + app + pgadmin
├── .env.example                # REDIS_HOST, REDIS_PORT
└── src/test/kotlin/.../
    ├── support/AbstractIntegrationTest.kt
    └── api/RestaurantCacheTest.kt
```

### Стек в docker-compose

```
postgres (healthcheck) ──┐
redis (healthcheck) ─────┼──→ app (Spring Boot + Redis Cache)
                         └──→ pgadmin
```

---

## Реализованное в ЛР-9

### 1. Redis в docker-compose

**Файл:** `docker-compose.yaml`

```yaml
redis:
  image: redis:7-alpine
  ports:
    - "6379:6379"
  volumes:
    - redis_data:/data
  command: redis-server --appendonly yes
  healthcheck:
    test: ["CMD", "redis-cli", "ping"]
    interval: 5s
    timeout: 3s
    retries: 5

app:
  environment:
    REDIS_HOST: redis
    REDIS_PORT: 6379
  depends_on:
    redis:
      condition: service_healthy
```

**Файл:** `.env.example`

```
REDIS_HOST=localhost
REDIS_PORT=6379
```

---

### 2. Зависимости и конфигурация

**Файл:** `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

**Файл:** `Lab3Application.kt`

```kotlin
@SpringBootApplication
@EnableCaching
class Lab3Application
```

**Файл:** `application.yml`

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  cache:
    type: redis
```

---

### 3. CacheConfig (JSON-сериализация, TTL)

**Файл:** `src/main/kotlin/com/example/lab3/config/CacheConfig.kt`

```kotlin
@Configuration
class CacheConfig {
    @Bean
    fun redisCacheManagerBuilderCustomizer(objectMapper: ObjectMapper): RedisCacheManagerBuilderCustomizer {
        val serializer = GenericJackson2JsonRedisSerializer(objectMapper)
        val pair = RedisSerializationContext.SerializationPair.fromSerializer(serializer)

        fun config(ttl: Duration) = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ttl)
            .serializeValuesWith(pair)
            .disableCachingNullValues()

        return RedisCacheManagerBuilderCustomizer { builder ->
            builder
                .withCacheConfiguration("restaurants", config(Duration.ofHours(1)))
                .withCacheConfiguration("dishes", config(Duration.ofHours(1)))
                .cacheDefaults(config(Duration.ofMinutes(5)))
        }
    }
}
```

---

### 4. @Cacheable на read-методах

**Файл:** `RestaurantService.kt`

```kotlin
@Cacheable(cacheNames = ["restaurants"])
fun findAll(): List<Restaurant> {
    logger.info { "Loading all restaurants from DB" }
    return restaurantRepositoryPort.findAll()
}

@Cacheable(cacheNames = ["restaurants"], key = "#id")
fun findById(id: Long): Restaurant? {
    logger.info { "Loading restaurant id=$id from DB" }
    return restaurantRepositoryPort.findById(id)
}
```

**Файл:** `DishService.kt`

```kotlin
@Cacheable(cacheNames = ["dishes"], key = "#restaurantId")
fun findByRestaurantId(restaurantId: Long): List<Dish> {
    logger.info { "Loading dishes for restaurantId=$restaurantId from DB" }
    return dishRepositoryPort.findByRestaurantId(restaurantId)
}
```

---

### 5. Инвалидация кэша

| Операция              | Аннотация |
|:----------------------|:----------|
| create restaurant     | `@CacheEvict(restaurants, allEntries=true)` |
| update restaurant     | `@CachePut(restaurants, key="#id")` |
| delete restaurant     | `@CacheEvict` restaurants + dishes (allEntries) |
| create/update dish    | `@CacheEvict(dishes, key=restaurantId)` |
| delete dish           | `@CacheEvict` dishes + restaurants |

---

### 6. Тесты кэширования

**Файл:** `src/test/kotlin/com/example/lab3/api/RestaurantCacheTest.kt`

- Повторный `GET /api/v1/restaurants` — запись в `cacheManager` сохраняется
- После `POST /api/v1/restaurants` — кэш `restaurants` пуст
- После `PUT` — кэш содержит обновлённые данные (`@CachePut`)
- После `DELETE` блюда — сброшены кэши `dishes` и `restaurants`

Redis в тестах поднимается через Testcontainers (singleton в `AbstractIntegrationTest`).

---

## Запуск

### Весь стек

```bash
cp .env.example .env   # заполнить значения
docker compose up --build
```

### Локально (БД + Redis в Docker, app через Maven)

```bash
docker compose up -d postgres redis
./mvnw spring-boot:run
```

### Проверка кэша в redis-cli

```bash
docker compose exec redis redis-cli KEYS *
docker compose exec redis redis-cli GET "restaurants::SimpleKey []"
docker compose exec redis redis-cli TTL "restaurants::SimpleKey []"
```

### Автотесты

```bash
./mvnw test
```

---

## Скриншоты
<img width="351" height="40" alt="image" src="https://github.com/user-attachments/assets/a0a76546-53ce-4c63-a951-58b8a629be0d" />
<img width="1007" height="627" alt="image" src="https://github.com/user-attachments/assets/3c61cd75-1200-41ca-bbae-9bfd2cb072a0" />
<img width="319" height="59" alt="image" src="https://github.com/user-attachments/assets/636456df-1b2a-4a3b-9d8d-f57f07a77bdf" />
<img width="507" height="224" alt="image" src="https://github.com/user-attachments/assets/dd27edb1-3d02-42d0-aa7c-518dfa2b15f9" />
<img width="697" height="34" alt="image" src="https://github.com/user-attachments/assets/feb94c28-753f-4130-8b33-85a0345a027e" />



---

## CI / CD

Без изменений относительно ЛР-8: `ci.yaml` (PR → тесты), `cd.yaml` (push → Docker-образ в GHCR).

---

## Эндпоинты (кратко)

| Метод  | Путь                              | Кэш              |
|:-------|:----------------------------------|:-----------------|
| GET    | `/api/v1/restaurants`             | `restaurants`    |
| GET    | `/api/v1/restaurants/{id}`        | `restaurants`    |
| GET    | `/api/v1/restaurants/{id}/dishes` | `dishes`         |
| POST   | `/api/v1/restaurants`             | инвалидация      |
| POST   | `/api/v1/restaurants/{id}/dishes` | инвалидация menu |

Полное описание API — в Swagger UI: http://localhost:8080/swagger-ui.html
