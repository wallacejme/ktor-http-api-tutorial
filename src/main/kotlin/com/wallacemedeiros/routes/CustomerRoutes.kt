package com.wallacemedeiros.routes

import com.wallacemedeiros.dao.customerDAO
import com.wallacemedeiros.models.PostCustomer
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.customerRouting() {
    route("/customer") {
        get {
            call.respond(customerDAO.all())
        }
        get("{id?}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respondText(
                "Id inválido",
                status = HttpStatusCode.BadRequest
            )
            val customer =
                customerDAO.getOne(id) ?: return@get call.respondText(
                    "Nenhum cliente com id $id",
                    status = HttpStatusCode.NotFound
                )
            call.respond(customer)
        }
        post {
            val body = call.receive<PostCustomer>()
            val createdCustomer = customerDAO.addNew(body.firstName, body.lastName, body.email) ?: return@post call.respondText(
                "Cliente inválido",
                status = HttpStatusCode.BadRequest
            )

            call.respond(
                status = HttpStatusCode.Created,
                createdCustomer
            )
        }
        put("{id?}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
            val body = call.receive<PostCustomer>()

            customerDAO.edit(id, body.firstName, body.lastName, body.email)

            call.respond(HttpStatusCode.NoContent)
        }
        delete("{id?}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            customerDAO.delete(id)
            call.respondText("", status = HttpStatusCode.NoContent)
        }
    }
}