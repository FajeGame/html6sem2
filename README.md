# Лабораторная работа №11

**Тема:** брокер сообщений RabbitMQ, событийная архитектура

---

## Описание проекта

REST API сервиса доставки еды — продолжение ЛР-4–10. В текущей работе прямой вызов `NotificationService` из `OrderService` заменён на публикацию доменных событий в RabbitMQ. Email-уведомления обрабатываются асинхронно через `@RabbitListener`.

```
OrderService → RabbitMQ (order.exchange) → NotificationConsumer → NotificationService → MailHog
```

Management UI: http://localhost:15672 (guest / guest)

---

## Структура проекта

```
lab11/
├── src/main/kotlin/com/example/lab3/
│   ├── config/RabbitConfig.kt           # exchange, queues, bindings, DLQ, JSON
│   ├── domain/event/                    # OrderCreatedEvent, OrderStatusChangedEvent
│   ├── application/
│   │   ├── OrderEventPublisher.kt       # RabbitTemplate
│   │   ├── NotificationConsumer.kt      # @RabbitListener
│   │   └── OrderService.kt              # без зависимости на NotificationService
│   └── infrastructure/jpa/
│       └── ProcessedEventEntity.kt      # идемпотентность
├── docker-compose.yaml                  # + rabbitmq:3-management
└── db/migration/V7__create_processed_events.sql
```

---

## Реализованное в ЛР-11

### 1. RabbitMQ в docker-compose

**Файл:** `docker-compose.yaml`

```yaml
rabbitmq:
  image: rabbitmq:3-management
  ports:
    - "5672:5672"
    - "15672:15672"
  healthcheck:
    test: ["CMD", "rabbitmq-diagnostics", "ping"]
```

---

### 2. RabbitConfig

**Файл:** `src/main/kotlin/com/example/lab3/config/RabbitConfig.kt`

- Topic exchange `order.exchange`
- Очереди: `order.status.changed.queue`, `order.created.queue`
- DLQ: `order.status.changed.dlq` (через `x-dead-letter-*`)
- `Jackson2JsonMessageConverter`, durable-очереди

---

### 3. Доменные события

**Файл:** `domain/event/OrderStatusChangedEvent.kt`

```kotlin
data class OrderStatusChangedEvent(
    val orderId: Long,
    val userId: Long,
    val userEmail: String,
    val oldStatus: OrderStatus,
    val newStatus: OrderStatus,
    val changedAt: LocalDateTime = LocalDateTime.now()
)
```

---

### 4. OrderService — публикация вместо прямого вызова

**Файл:** `OrderService.kt` — зависит от `OrderEventPublisher`, **не** от `NotificationService`:

```kotlin
orderEventPublisher.publishOrderStatusChanged(
    OrderStatusChangedEvent(
        orderId = updated.id,
        userId = updated.userId,
        userEmail = user.email,
        oldStatus = existing.status,
        newStatus = newStatus
    )
)
```

---

### 5. NotificationConsumer

**Файл:** `NotificationConsumer.kt`

```kotlin
@RabbitListener(queues = [RabbitConfig.ORDER_STATUS_QUEUE])
fun handleOrderStatusChanged(event: OrderStatusChangedEvent) {
    if (processedEventRepository.existsByOrderIdAndNewStatus(...)) return
    notificationService.sendStatusChangedEmail(event)
    processedEventRepository.save(...)
}

@RabbitListener(queues = [RabbitConfig.ORDER_CREATED_QUEUE])
fun handleOrderCreated(event: OrderCreatedEvent) { ... }
```

---

### 6. Идемпотентность

**Файл:** `V7__create_processed_events.sql` — уникальный индекс `(order_id, new_status)`.

---

## Запуск

```bash
cp .env.example .env
docker compose up --build
```

| Сервис | URL |
|:-------|:----|
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |
| RabbitMQ UI | http://localhost:15672 (guest/guest) |
| MailHog | http://localhost:8025 |

### Автотесты

```bash
./mvnw test
```
---
## Скриншоты


---

## CI / CD

Без изменений относительно ЛР-8–10.

---

## Эндпoинты (кратко)

| Метод | Путь | Событие в RabbitMQ |
|:------|:-----|:-------------------|
| POST | `/api/v1/orders` | `order.created` |
| PATCH | `/api/v1/orders/{id}/status` | `order.status.changed` |
