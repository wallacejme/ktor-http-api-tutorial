package com.wallacemedeiros.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.wallacemedeiros.models.PostUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.authenticationRouting() {
    post("/login") {
        val user = call.receive<PostUser>()

        // Check username and password
        if (user.username != "test_user" || user.password != "password") {
            return@post call.respondText(
                "Usuário inválido",
                status = HttpStatusCode.Unauthorized
            )
        }

        val secret = application.environment.config.property("jwt.secret").getString()
        val issuer = application.environment.config.property("jwt.issuer").getString()
        val audience = application.environment.config.property("jwt.audience").getString()
        val token = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", user.username)
            .withExpiresAt(Date(System.currentTimeMillis() + 60000))
            .sign(Algorithm.HMAC256(secret))
        call.respond(hashMapOf("token" to token))
    }
}