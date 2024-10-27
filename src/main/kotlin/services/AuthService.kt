package org.example.services

import org.example.routes.LoginRequestDto
import java.util.Date
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.auth.jwt.JWTCredential
import org.example.dtos.UserDto
import org.example.dtos.UserPrincipal

class AuthService(
    private val jwtSecret: String,
    private val jwtIssuer: String,
    private val jwtAudience: String,
    val jwtRealm: String,
    private val userService: UserService
) {
    constructor(application: Application, userService: UserService) : this(
        jwtSecret = application.environment.config.property("jwt.secret").getString(),
        jwtIssuer = application.environment.config.property("jwt.issuer").getString(),
        jwtAudience = application.environment.config.property("jwt.audience").getString(),
        jwtRealm = application.environment.config.property("jwt.realm").getString(),
        userService = userService
    )

    val jwtVerifier: JWTVerifier = JWT
        .require(Algorithm.HMAC256(jwtSecret))
        .withAudience(jwtAudience)
        .withIssuer(jwtIssuer)
        .build()

    suspend fun authenticate(loginRequest: LoginRequestDto): String? {
        val foundUser = userService.authenticate(loginRequest.email, loginRequest.password)

        if (foundUser == null)
            return null

        return createAccessToken(foundUser)
    }

    // A credential is a set of properties for a server to authenticate a principal (here a JWTCredential)
    // A principal is an entity that can be authenticated (here a JWTPrincipal)
    suspend fun customValidator(credential: JWTCredential): UserPrincipal? {
        val id = getId(credential) ?: return null
        val foundUser = userService.find(id)

        return foundUser?.let { user ->
            if (!audienceMatches(credential))
                return null

            return UserPrincipal(user)
        }
    }

    private fun audienceMatches(credential: JWTCredential): Boolean {
        return credential.payload.audience.contains(jwtAudience)
    }

    private fun getId(credential: JWTCredential): Long? {
        return credential.payload.getClaim("id").asLong()
    }

    private fun createAccessToken(foundUser: UserDto): String = JWT
        .create()
        .withAudience(jwtAudience)
        .withIssuer(jwtIssuer)
        .withClaim("id", foundUser.id)
        // note when getting a claim of type String use claim.asString() instead of claim.toString()
        .withClaim("userType", foundUser.userType)
        .withClaim("email", foundUser.email)
        .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000)) // with this the JWT is also unique after each new request
        .sign(Algorithm.HMAC256(jwtSecret))
}