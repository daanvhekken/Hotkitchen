package org.example

import org.example.plugins.*
import org.example.repositories.*
import org.example.routes.*
import org.example.services.*
import io.ktor.server.application.*
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.mainModule() {
    val database = DatabaseInit.init()

    configureDependencies(database)
    configureSerialization()

    val authService: AuthService = get()
    val userService: UserService = get()
    val mealService: MealService = get()
    val categoryService: CategoryService = get()
    val orderService: OrderService = get()

    setupJwt(userService)

    routing {
        securityRoutes(userService, authService)
        meRoutes(userService)
        mealRoutes(mealService, categoryService)
        categoryRoutes(categoryService)
        orderRoutes(orderService, mealService, userService)
    }
}

fun Application.configureDependencies(database: Database) {
    install(Koin) {
        modules(module(createdAtStart = true, fun Module.() {
            single<UserRepository> { UserRepositoryImpl(database) }
            single<CategoryRepository> { CategoryRepositoryImpl(database) }
            single<MealRepository> { MealRepositoryImpl(database) }
            single<OrderRepository> { OrderRepositoryImpl(database) }

            single<UserService> { UserService(get()) }
            single<AuthService> { AuthService(this@configureDependencies, get()) }
            single<MealService> { MealService(get()) }
            single<CategoryService> { CategoryService(get()) }
            single<OrderService> { OrderService(get()) }
        }))
    }
}