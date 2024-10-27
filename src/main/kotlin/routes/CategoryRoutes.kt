package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.example.dtos.CreateCategoryDTO
import org.example.plugins.currentUserUserType
import org.example.services.CategoryService

fun Route.categoryRoutes(
    categoryService: CategoryService
) {
    authenticate("auth-jwt") {
        get("/categories") {
            val categories = categoryService.findAll()
            call.respond(HttpStatusCode.OK, categories)
        }

        get("/categories/{id}") {
            val id = call.parameters["id"]!!.toLong()

            val category = categoryService.find(id)
            if (category == null) {
                return@get call.respond(HttpStatusCode.BadRequest)
            }

            call.respond(HttpStatusCode.OK, category)
        }

        post("/categories") {
            if (currentUserUserType() != "staff") {
                return@post call.respond(HttpStatusCode.Forbidden, mapOf("status" to "Access denied"))
            }

            val category = call.receive<CreateCategoryDTO>()

            val id = categoryService.create(category)
            val createCategory = categoryService.find(id)

            if (createCategory == null) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }

            call.respond(HttpStatusCode.OK, createCategory)
        }
    }
}