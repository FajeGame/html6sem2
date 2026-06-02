package com.example.lab3.application

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component

@Component
class OrderMetrics(private val meterRegistry: MeterRegistry) {

    val processingDuration: Timer = Timer.builder("order_processing_duration_seconds")
        .description("Длительность обработки запроса на создание заказа")
        .publishPercentileHistogram()
        .register(meterRegistry)

    val ordersCreated: Counter = Counter.builder("orders_created_total")
        .description("Количество успешно созданных заказов")
        .register(meterRegistry)

    fun recordBusinessError(type: String) =
        meterRegistry.counter("business_errors_total", "type", type).increment()

    fun recordOrderCreationFailed(reason: String) =
        meterRegistry.counter("order_creation_failed_total", "reason", reason).increment()
}
