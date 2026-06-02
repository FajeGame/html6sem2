package com.example.lab3.infrastructure.jpa

import com.example.lab3.domain.Role
import com.example.lab3.domain.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(nullable = false, unique = true)
    var email: String = "",
    @Column(nullable = false)
    var firstName: String = "",
    @Column(nullable = false)
    var lastName: String = "",
    @Column(nullable = false)
    var isActive: Boolean = true,
    @Column(nullable = false)
    var password: String = "",
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER
) {
    fun toDomain(): User = User(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        isActive = isActive,
        role = role
    )

    companion object {
        fun fromDomain(user: User, password: String? = null): UserEntity = UserEntity(
            id = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            isActive = user.isActive,
            password = password ?: user.passwordHash ?: "",
            role = user.role
        )
    }
}
