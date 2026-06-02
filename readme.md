<img width="1074" height="262" alt="image" src="https://github.com/user-attachments/assets/899f03ed-7326-4231-8776-09a5268faa24" /># Лабораторная работа №12

**Тема:** мониторинг — Actuator, Prometheus, Grafana, Loki, Grafana Alloy

---

## Описание проекта

REST API сервиса доставки еды — продолжение ЛР-4–11. Добавлен observability-стек: метрики приложения через Micrometer/Prometheus, метрики контейнеров через cAdvisor, визуализация в Grafana, агрегация логов Docker-контейнеров в Loki через Alloy.

```
Spring Boot → /actuator/prometheus → Prometheus → Grafana
Docker logs → Alloy → Loki → Grafana Explore
cAdvisor → Prometheus → Grafana
```

| Сервис | URL |
|:-------|:----|
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin / admin) |
| cAdvisor | http://localhost:8088 |
| Loki | http://localhost:3100 |
| Alloy UI | http://localhost:12345 |

---

## Структура проекта

```
lab12/
├── monitoring/
│   ├── prometheus.yml
│   ├── loki-config.yaml
│   ├── alloy-config.alloy
│   └── grafana/
│       ├── provisioning/datasources/datasources.yaml
│       ├── provisioning/dashboards/dashboards.yaml
│       └── dashboards/
│           ├── cadvisor.json
│           ├── jvm.json
│           └── business.json
├── src/main/kotlin/com/example/lab3/application/
│   ├── OrderMetrics.kt
│   └── OrderService.kt          # замеры в create()
└── docker-compose.yaml          # + prometheus, cadvisor, grafana, loki, alloy
```

---

## Реализованное в ЛР-12

### 1. Actuator и Prometheus registry

**Файл:** `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Файл:** `application.yml` — экспорт `health`, `info`, `prometheus`, `metrics`; probes; гистограмма HTTP.

---

### 2. OrderMetrics — четыре кастомные метрики

**Файл:** `OrderMetrics.kt`

| Метрика | Тип | Теги |
|:--------|:----|:-----|
| `order_processing_duration_seconds` | Timer | — |
| `orders_created_total` | Counter | — |
| `business_errors_total` | Counter | `type` (validation) |
| `order_creation_failed_total` | Counter | `reason` (stock_empty) |

---

### 3. Замеры в OrderService.create

**Файл:** `OrderService.kt`

- Пустой `dishIds` / пользователь не найден / блюда не найдены → `business_errors_total{type="validation"}`
- Блюдо с `isAvailable = false` → `order_creation_failed_total{reason="stock_empty"}`
- Успешное создание → `orders_created_total++`, длительность в `order_processing_duration_seconds`

---

### 4. Security: Actuator без JWT

**Файл:** `SecurityConfig.kt`

```kotlin
it.requestMatchers("/actuator/**").permitAll()
```

---

### 5. Prometheus scrape

**Файл:** `monitoring/prometheus.yml`

- `spring` → `app:8080/actuator/prometheus`
- `cadvisor` → `cadvisor:8080`

---

### 6. Loki + Alloy

**Файлы:** `loki-config.yaml`, `alloy-config.alloy` — сбор stdout/stderr всех Docker-контейнеров с лейблом `compose_service`.

---

### 7. Grafana provisioning

Datasources: Prometheus (default), Loki. Дашборды: cAdvisor (14282), JVM Micrometer (4701), business (4 панели по кастомным метрикам).

---

## Запуск

```bash
cp .env.example .env
# заполните POSTGRES_*, JWT_SECRET, PGADMIN_*
docker compose up --build -d
```

Проверка метрик:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/prometheus
```

### Автотесты

```bash
mvn test
```

44 теста (включая ЛР-6–11).

---

## CI / CD

Без изменений относительно ЛР-8–11.


---
## Скриншоты
<img width="296" height="21" alt="image" src="https://github.com/user-attachments/assets/6de23507-3049-4821-bba8-61e03928f7f3" />
<img width="826" height="491" alt="image" src="https://github.com/user-attachments/assets/2bd0fae0-e744-4504-bf56-81bd31587e9e" />
<img width="1268" height="489" alt="image" src="https://github.com/user-attachments/assets/8acc114d-30fb-48a2-b00d-39eaeb7d9b7b" />
<img width="1275" height="486" alt="image" src="https://github.com/user-attachments/assets/c551c0d2-aaba-407d-8af7-929538b8cef3" />
<img width="1074" height="262" alt="image" src="https://github.com/user-attachments/assets/76158ef8-377e-4eb5-a3b8-4d70d7fd2e07" />
<img width="1091" height="476" alt="image" src="https://github.com/user-attachments/assets/dc2e7176-b266-4ee1-96cd-56c5efa43b4e" />
![Uploading image.png…]()

---

## Эндпоинты Actuator

| Путь | Назначение |
|:-----|:-----------|
| `/actuator/health` | Liveness/readiness |
| `/actuator/prometheus` | Метрики для Prometheus |
| `/actuator/info` | Имя и версия приложения |
