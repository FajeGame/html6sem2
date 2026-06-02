package com.example.lab3.application

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val mailSender: JavaMailSender,
    private val scope: CoroutineScope
) {
    private val logger = KotlinLogging.logger {}

    fun sendOrderStatusUpdate(to: String, orderId: Long, status: String) {
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    mailSender.send(
                        SimpleMailMessage().apply {
                            setTo(to)
                            subject = "Заказ #$orderId: статус изменён"
                            text = "Новый статус заказа #$orderId: $status"
                        }
                    )
                }
            }.onSuccess {
                logger.info { "Notification for order #$orderId sent to $to" }
            }.onFailure { ex ->
                logger.error(ex) { "Failed to send notification for order #$orderId to $to" }
            }
        }
    }
}
