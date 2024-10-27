package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.example.dtos.CreateUserDto
import org.example.services.AuthService
import org.example.services.UserService

@Serializable
class LoginRequestDto(
    val email: String,
    val password: String
)

fun Route.securityRoutes(
    userService: UserService,
    authService: AuthService
) {
    post("/signup") {
        val payload = call.receive<CreateUserDto>()

        // validate if the payload.email is an actual e-mail address
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        if (!emailRegex.matches(payload.email)) {
            return@post call.respond(HttpStatusCode.Forbidden, mapOf("status" to "Invalid email"))
        }

        // valid password is a password that is at least six characters long and consists of letters and numbers
        val validPassword = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$".toRegex()
        if (!validPassword.matches(payload.password)) {
            return@post call.respond(HttpStatusCode.Forbidden, mapOf("status" to "Invalid password"))
        }

        val existingUser = userService.findByEmail(payload.email)
        if (existingUser != null) {
            return@post call.respond(HttpStatusCode.Forbidden, mapOf("status" to "User already exists"))
        } else {
            // Registration is a go, create user, get the token and then respond with the token
            userService.create(CreateUserDto(
                payload.email,
                payload.userType,
                payload.password,
                payload.name,
                payload.phone,
                payload.address
            ))

            val token = authService.authenticate(LoginRequestDto(payload.email, payload.password))
            return@post call.respond(HttpStatusCode.OK, mapOf("token" to token))

        }
    }

    post("/signin") {
        val payload = call.receive<LoginRequestDto>()

        val token = authService.authenticate(payload)

        if (token == null) {
            call.respond(HttpStatusCode.Forbidden, mapOf("status" to "Invalid email or password"))
        } else {
            call.respond(HttpStatusCode.OK, mapOf("token" to token))
        }
    }

    authenticate("auth-jwt") {
        get("/validate") {
            val principal = call.principal<JWTPrincipal>()
            if (principal != null) {
                val email = principal.payload.getClaim("email").asString()
                val userType = principal.payload.getClaim("userType").asString()
                call.respond(HttpStatusCode.OK, mapOf("status" to "Hello, $userType $email"))
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}
