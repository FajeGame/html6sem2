package com.example.lab3.config

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoroutineConfig {

    @Bean
    fun applicationScope(): CoroutineScope {
        val handler = CoroutineExceptionHandler { _, ex ->
            LoggerFactory.getLogger("CoroutineScope")
                .error("Unhandled coroutine exception", ex)
        }
        return CoroutineScope(SupervisorJob() + Dispatchers.IO + handler)
    }
}
