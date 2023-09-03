package com.wallacemedeiros

import com.wallacemedeiros.dao.DatabaseFactory
import com.wallacemedeiros.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)
    configureSerialization()
    configureAuthentication()
    configureRouting()
    configureLogging()
}
