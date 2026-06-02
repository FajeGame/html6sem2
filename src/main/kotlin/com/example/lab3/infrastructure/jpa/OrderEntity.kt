package com.example.lab3.infrastructure.jpa

import com.example.lab3.domain.Order
import com.example.lab3.domain.OrderStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity = UserEntity(),
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,
    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @ManyToMany
    @JoinTable(
        name = "order_dishes",
        joinColumns = [JoinColumn(name = "order_id")],
        inverseJoinColumns = [JoinColumn(name = "dish_id")]
    )
    var dishes: MutableSet<DishEntity> = linkedSetOf()
) {
    fun toDomain(): Order = Order(
        id = id,
        userId = user.id,
        status = status,
        createdAt = createdAt,
        dishIds = dishes.map { it.id }.sorted()
    )
}
