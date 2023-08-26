package com.wallacemedeiros

import com.wallacemedeiros.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureAuthentication()
    configureRouting()
}
