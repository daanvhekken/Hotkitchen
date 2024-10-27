package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.example.dtos.CreateOrderDTO
import org.example.plugins.currentUserId
import org.example.plugins.currentUserUserType
import org.example.services.*

@Serializable
data class CreateOrderStartDTO(
    val mealIds: List<Long>
)

fun Route.orderRoutes(
    orderService: OrderService,
    mealService: MealService,
    userService: UserService,
) {
    authenticate("auth-jwt") {
        get("/orderHistory") {
            val orders = orderService.findAll()
            call.respond(HttpStatusCode.OK, orders)
        }

        get("/orderIncomplete") {
            val orders = orderService.findAllIncomplete()
            call.respond(HttpStatusCode.OK, orders)
        }

        post("/order") {
            val (mealIds) = call.receive<CreateOrderStartDTO>()

            val currentUser = userService.find(currentUserId())
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            val meals = mealIds.mapNotNull { mealService.find(it) }
            if (meals.size != mealIds.size) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }

            val totalPrice = meals.sumOf { it.price.toDouble() }.toFloat()

            val order = CreateOrderDTO(
                userEmail = currentUser.email,
                mealIds = mealIds,
                price = totalPrice,
                userAddress = currentUser.address,
                status = "IN PROGRESS"
            )

            orderService.create(order)
                .let { orderService.find(it) }
                ?.let { createdOrder ->
                    call.respond(HttpStatusCode.Created, createdOrder)
                } ?: return@post call.respond(HttpStatusCode.InternalServerError, "Failed to create order")
        }

        post("/order/{id}/markReady") {
            if (currentUserUserType() != "staff") {
                return@post call.respond(HttpStatusCode.Forbidden, mapOf("status" to "Access denied"))
            }

            val id = call.parameters["id"]!!.toLong()
            orderService.find(id)
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            orderService.markReady(id)

            val order = orderService.find(id)
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            call.respond(HttpStatusCode.OK, order)
        }
    }
}