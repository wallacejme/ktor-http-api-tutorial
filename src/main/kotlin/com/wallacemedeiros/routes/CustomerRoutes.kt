package com.wallacemedeiros.routes

import com.wallacemedeiros.models.Customer
import com.wallacemedeiros.models.customerStorage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.customerRouting() {
    route("/customer") {
        get {
            if (customerStorage.isNotEmpty()) {
                call.respond(customerStorage)
            } else {
                call.respondText("Nenhum cliente encontrado.", status = HttpStatusCode.OK)
            }
        }
        get("{id?}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "Id não informado",
                status = HttpStatusCode.BadRequest
            )
            val customer =
                customerStorage.find { it.id == id.toIntOrNull() } ?: return@get call.respondText(
                    "Nenhum cliente com id $id",
                    status = HttpStatusCode.NotFound
                )
            call.respond(customer)
        }
        post {
            val customer = call.receive<Customer>()
            customerStorage.add(customer)
            call.respondText("Cliente criado com sucesso", status = HttpStatusCode.Created)
        }
        delete("{id?}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (customerStorage.removeIf { it.id == id.toIntOrNull() }) {
                call.respondText("Cliente removido com sucesso", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Cliente não encontrado", status = HttpStatusCode.NotFound)
            }
        }
    }
}