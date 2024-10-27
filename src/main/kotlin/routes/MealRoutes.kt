package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.example.dtos.CreateMealDTO
import org.example.plugins.currentUserUserType
import org.example.services.*

fun Route.mealRoutes(
    mealService: MealService,
    categoryService: CategoryService
) {
    authenticate("auth-jwt") {
        get("/meals") {
            val meals = mealService.findAll()
            call.respond(HttpStatusCode.OK, meals)
        }

        // get one meal with id query param
        get("/meals/{id}") {
            val id = call.parameters["id"]!!.toLong()

            val meal = mealService.find(id)
            if (meal == null) {
                return@get call.respond(HttpStatusCode.BadRequest)
            }

            call.respond(HttpStatusCode.OK, meal)
        }

        post("/meals") {
            if (currentUserUserType() != "staff") {
                return@post call.respond(HttpStatusCode.Forbidden, mapOf("status" to "Access denied"))
            }

            val meal = call.receive<CreateMealDTO>()

            // check if each category id exists
            meal.categoryIds.forEach {
                if (categoryService.find(it) == null) {
                    return@post call.respond(HttpStatusCode.BadRequest)
                }
            }

            val id = mealService.create(meal)
            val createdMeal = mealService.find(id)

            if (createdMeal == null) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }

            call.respond(HttpStatusCode.OK, createdMeal)
        }
    }
}