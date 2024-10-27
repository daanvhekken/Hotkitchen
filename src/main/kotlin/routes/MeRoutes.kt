package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.*
import org.example.dtos.UpdateUserDto
import org.example.services.UserService

fun Route.meRoutes(
    userService: UserService
) {
    authenticate("auth-jwt") {
        get("/me") {
            call.respondText("Hello, world!")
            val principal = call.principal<JWTPrincipal>()
            if (principal != null) {
                val id = principal.payload.getClaim("id").asLong()
                val user = userService.find(id)

                if (user !== null) {
                    val userMap = mapOf(
                        "name" to user.name,
                        "userType" to user.userType,
                        "phone" to user.phone,
                        "email" to user.email,
                        "address" to user.address
                    )

                    call.respond(HttpStatusCode.OK, userMap)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

        put("/me") {
            val principal = call.principal<JWTPrincipal>()
            if (principal != null) {
                val id = principal.payload.getClaim("id").asLong()
                val user = userService.find(id)

                if (user !== null) {
                    val payload = call.receive<UpdateUserDto>()
                    userService.update(user.id, payload)
                    return@put call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

        delete("/me") {
            val principal = call.principal<JWTPrincipal>()
            if (principal != null) {
                val id = principal.payload.getClaim("id").asLong()
                val user = userService.find(id)

                if (user !== null) {
                    userService.delete(user.id)
                    return@delete call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}