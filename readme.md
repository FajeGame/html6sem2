# Лабораторная работа №10

**Тема:** асинхронность и планировщик (корутины, @Scheduled, Spring Mail)

---

## Описание проекта

REST API сервиса доставки еды — продолжение ЛР-4–9: JWT, Redis-кэш, Docker, CI/CD. В текущей работе добавлены асинхронные email-уведомления при смене статуса заказа (Kotlin coroutines) и планировщик для автоматической отмены «зависших» заказов в статусе `PREPARING`.

Для локальной разработки используется **MailHog** — письма не уходят в интернет, а отображаются в веб-UI.

---

## Структура проекта

```
lab10/
├── src/main/kotlin/com/example/lab3/
│   ├── config/CoroutineConfig.kt       # applicationScope (SupervisorJob + IO)
│   ├── application/
│   │   ├── NotificationService.kt      # scope.launch + withContext(IO)
│   │   ├── OrderScheduler.kt           # @Scheduled отмена зависших заказов
│   │   └── OrderService.kt             # вызов NotificationService
│   └── ...
├── docker-compose.yaml                 # + mailhog (1025 SMTP, 8025 UI)
└── .env.example                        # MAIL_HOST, STUCK_ORDER_*
```

---

## Реализованное в ЛР-10

### 1. CoroutineConfig

**Файл:** `src/main/kotlin/com/example/lab3/config/CoroutineConfig.kt`

```kotlin
@Bean
fun applicationScope(): CoroutineScope {
    val handler = CoroutineExceptionHandler { _, ex ->
        LoggerFactory.getLogger("CoroutineScope").error("Unhandled coroutine exception", ex)
    }
    return CoroutineScope(SupervisorJob() + Dispatchers.IO + handler)
}
```

---

### 2. NotificationService

**Файл:** `src/main/kotlin/com/example/lab3/application/NotificationService.kt`

```kotlin
fun sendOrderStatusUpdate(to: String, orderId: Long, status: String) {
    scope.launch {
        runCatching {
            withContext(Dispatchers.IO) {
                mailSender.send(SimpleMailMessage().apply {
                    setTo(to)
                    subject = "Заказ #$orderId: статус изменён"
                    text = "Новый статус заказа #$orderId: $status"
                })
            }
        }.onFailure { ex ->
            logger.error(ex) { "Failed to send notification..." }
        }
    }
}
```

Вызывается из `OrderService.updateStatus` после успешного обновления.

---

### 3. Spring Mail + MailHog

**Файл:** `docker-compose.yaml`

```yaml
mailhog:
  image: mailhog/mailhog
  ports:
    - "1025:1025"   # SMTP
    - "8025:8025"   # Web UI
```

**Файл:** `application.yml`

```yaml
spring:
  mail:
    host: ${MAIL_HOST:localhost}
    port: ${MAIL_PORT:1025}

app:
  scheduler:
    stuck-order-interval-ms: ${STUCK_ORDER_INTERVAL_MS:900000}
    stuck-order-threshold-hours: ${STUCK_ORDER_THRESHOLD_HOURS:1}
```

---

### 4. OrderScheduler

**Файл:** `src/main/kotlin/com/example/lab3/application/OrderScheduler.kt`

```kotlin
@Scheduled(fixedDelayString = "\${app.scheduler.stuck-order-interval-ms}")
fun cancelStuckOrders() {
    val threshold = LocalDateTime.now().minusHours(thresholdHours)
    val stuck = orderService.findStuckPreparingOrders(threshold)
    logger.info { "Found ${stuck.size} stuck orders in PREPARING status" }
    stuck.forEach { order ->
        orderService.updateStatus(order.id, OrderStatus.CANCELLED)
        logger.info { "Cancelled stuck order id=${order.id}" }
    }
}
```

Добавлен статус `PREPARING` в `OrderStatus`. Цепочка: `PENDING → CONFIRMED → PREPARING → DELIVERED`.

---

### 5. Тесты

- `OrderServiceTest` — проверка вызова `NotificationService` при смене статуса
- `OrderSchedulerTest` — отмена зависших заказов
- Планировщик отключён в тестах: `@Profile("!test")` на `OrderScheduler`

---

## Запуск

```bash
cp .env.example .env   # заполнить значения
docker compose up --build
```

| Сервис   | URL |
|:---------|:----|
| API      | http://localhost:8080 |
| Swagger  | http://localhost:8080/swagger-ui.html |
| MailHog  | http://localhost:8025 |
| pgAdmin  | http://localhost:5050 |

### Автотесты

```bash
./mvnw test
```

---

## CI / CD

Без изменений относительно ЛР-8–9.

---

## Скриншоты
<img width="739" height="76" alt="image" src="https://github.com/user-attachments/assets/2e7df058-9987-47fd-92ef-fa6d8f444ad4" />
<img width="810" height="253" alt="image" src="https://github.com/user-attachments/assets/6f1a1ca8-5b06-41d9-8a97-5b2db8c897cb" />
<img width="340" height="312" alt="image" src="https://github.com/user-attachments/assets/74e77679-020e-4962-a814-d096391b20ac" />
<img width="912" height="331" alt="image" src="https://github.com/user-attachments/assets/bf551f86-dd47-4d72-ba56-cc7987f9d21e" />


---

## Эндпоинты (кратко)

| Метод  | Путь                         | Действие                          |
|:-------|:-----------------------------|:----------------------------------|
| PATCH  | `/api/v1/orders/{id}/status` | Смена статуса + email пользователю |
| POST   | `/api/v1/orders`             | Создание заказа                   |

Статусы заказа: `PENDING`, `CONFIRMED`, `PREPARING`, `DELIVERED`, `CANCELLED`.
