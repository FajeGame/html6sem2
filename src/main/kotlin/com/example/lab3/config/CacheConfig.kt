package com.example.lab3.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import java.time.Duration

@Configuration
class CacheConfig {

    @Bean
    fun redisCacheManagerBuilderCustomizer(): RedisCacheManagerBuilderCustomizer {
        val redisObjectMapper = ObjectMapper().findAndRegisterModules().apply {
            val validator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Any::class.java)
                .build()
            activateDefaultTyping(validator, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY)
        }
        val serializer = GenericJackson2JsonRedisSerializer(redisObjectMapper)
        val serializationPair = RedisSerializationContext.SerializationPair.fromSerializer(serializer)

        fun config(ttl: Duration) = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ttl)
            .serializeValuesWith(serializationPair)
            .disableCachingNullValues()

        return RedisCacheManagerBuilderCustomizer { builder ->
            builder
                .withCacheConfiguration("restaurants", config(Duration.ofHours(1)))
                .withCacheConfiguration("dishes", config(Duration.ofHours(1)))
                .cacheDefaults(config(Duration.ofMinutes(5)))
        }
    }
}
