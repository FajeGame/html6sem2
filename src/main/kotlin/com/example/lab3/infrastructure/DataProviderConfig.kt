package com.example.lab3.infrastructure

import com.example.lab3.domain.DishRepositoryPort
import com.example.lab3.domain.OrderRepositoryPort
import com.example.lab3.domain.RestaurantRepositoryPort
import com.example.lab3.domain.UserRepositoryPort
import com.example.lab3.infrastructure.jpa.OrderJpaAdapter
import com.example.lab3.infrastructure.jpa.OrderJpaRepository
import com.example.lab3.infrastructure.jpa.RestaurantJpaAdapter
import com.example.lab3.infrastructure.jpa.RestaurantJpaRepository
import com.example.lab3.infrastructure.jpa.DishJpaAdapter
import com.example.lab3.infrastructure.jpa.DishJpaRepository
import com.example.lab3.infrastructure.jpa.UserJpaAdapter
import com.example.lab3.infrastructure.jpa.UserJpaRepository
import com.example.lab3.infrastructure.mock.MockOrderRepository
import com.example.lab3.infrastructure.mock.MockRestaurantRepository
import com.example.lab3.infrastructure.mock.MockDishRepository
import com.example.lab3.infrastructure.mock.MockUserRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataProviderConfig {
    @Bean
    @ConditionalOnProperty(name = ["app.data-provider"], havingValue = "mock")
    fun userMockRepository(): UserRepositoryPort = MockUserRepository()

    @Bean
    @ConditionalOnProperty(name = ["app.data-provider"], havingValue = "mock")
    fun dishMockRepository(): DishRepositoryPort = MockDishRepository()

    @Bean
    @ConditionalOnProperty(name = ["app.data-provider"], havingValue = "mock")
    fun restaurantMockRepository(): RestaurantRepositoryPort = MockRestaurantRepository()

    @Bean
    @ConditionalOnProperty(name = ["app.data-provider"], havingValue = "mock")
    fun orderMockRepository(): OrderRepositoryPort = MockOrderRepository()

    @Bean
    @ConditionalOnProperty(name = ["app.data-provider"], havingValue = "db", matchIfMissing = true)
    fun userJpaAdapter(userJpaRepository: UserJpaRepository): UserRepositoryPort = UserJpaAdapter(userJpaRepository)

    @Bean
    @ConditionalOnProperty(name = ["app.data-provider"], havingValue = "db", matchIfMissing = true)
    fun dishJpaAdapter(
        dishJpaRepository: DishJpaRepository,
        restaurantJpaRepository: RestaurantJpaRepository
    ): DishRepositoryPort = DishJpaAdapter(dishJpaRepository, restaurantJpaRepository)

    @Bean
    @ConditionalOnProperty(name = ["app.data-provider"], havingValue = "db", matchIfMissing = true)
    fun restaurantJpaAdapter(restaurantJpaRepository: RestaurantJpaRepository): RestaurantRepositoryPort =
        RestaurantJpaAdapter(restaurantJpaRepository)

    @Bean
    @ConditionalOnProperty(name = ["app.data-provider"], havingValue = "db", matchIfMissing = true)
    fun orderJpaAdapter(
        orderJpaRepository: OrderJpaRepository,
        userJpaRepository: UserJpaRepository,
        dishJpaRepository: DishJpaRepository
    ): OrderRepositoryPort = OrderJpaAdapter(orderJpaRepository, userJpaRepository, dishJpaRepository)
}
