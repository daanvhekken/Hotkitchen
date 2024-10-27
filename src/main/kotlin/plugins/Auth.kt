package org.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.http.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.example.dtos.UserDto
import java.util.*
import org.example.services.UserService

fun Application.setupJwt(
    userService: UserService
) {
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val myRealm = environment.config.property("jwt.realm").getString()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            validate { jwtCredential ->
                val result = userService.find(jwtCredential.payload.getClaim("id").asLong())
                if (result != null) {
                    JWTPrincipal(jwtCredential.payload)
                } else {
                    null
                }
            }

            challenge { defaultScheme, realm ->
                // userType from token:
                if (call.request.uri.endsWith("/me") && call.request.httpMethod.toString() === "Put") call.respond(HttpStatusCode.BadRequest)
                else if (call.request.uri.endsWith("/me") && call.request.httpMethod.toString() === "Delete") call.respond(HttpStatusCode.NotFound)
                else call.respond(HttpStatusCode.Unauthorized)
            }

            verifier(
                JWT.require(Algorithm.HMAC256(secret)).withAudience(audience).withIssuer(issuer).build()
            )
        }
    }
}

fun PipelineContext<Unit, ApplicationCall>.generateToken(user: UserDto, call: ApplicationCall): String {
    val config = call.application.environment.config
    val secret = config.property("jwt.secret").getString()
    val issuer = config.property("jwt.issuer").getString()
    val audience = config.property("jwt.audience").getString()

    return JWT.create().withAudience(audience).withIssuer(issuer).withClaim("id", user.id)
        .withExpiresAt(Date(System.currentTimeMillis() + 1_000 * 60 * 60 * 24)).sign(Algorithm.HMAC256(secret))
}

fun PipelineContext<Unit, ApplicationCall>.currentUserUserType(): String {
    return call.principal<JWTPrincipal>()!!.payload.getClaim("userType").asString()
}

fun PipelineContext<Unit, ApplicationCall>.currentUserId(): Long {
    return call.principal<JWTPrincipal>()!!.payload.getClaim("id").asLong()
}