package com.wallacemedeiros.plugins

import com.wallacemedeiros.routes.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        customerRouting()
        listOrderRouting()
        getOrderRoute()
        totalizeOrderRoute()
    }
}
