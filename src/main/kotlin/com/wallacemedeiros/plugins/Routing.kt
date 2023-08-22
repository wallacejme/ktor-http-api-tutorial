package com.wallacemedeiros.plugins

import com.wallacemedeiros.routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        customerRouting()
        listOrderRouting()
        getOrderRoute()
        totalizeOrderRoute()
    }
}
