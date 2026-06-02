package com.example.lab3

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class Lab3Application

fun main(args: Array<String>) {
    runApplication<Lab3Application>(*args)
}
