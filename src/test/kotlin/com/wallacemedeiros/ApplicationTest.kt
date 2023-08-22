package com.wallacemedeiros

import com.wallacemedeiros.models.Customer
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.*

class CustomerRouteTest {
    private val luizGonzaga = Customer("1", "Luiz", "Gonzaga", "luiz.gonzaga@musica.com")
    @Test
    fun testCreateCustomer() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val response = client.post("/customer") {
            contentType(ContentType.Application.Json)
            setBody(luizGonzaga)
        }
        assertEquals("Cliente criado com sucesso", response.bodyAsText())
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun testGetCustomer() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val response = client.get("/customer/${luizGonzaga.id}")
        assertEquals(luizGonzaga, response.body())
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testGetNonExistentCustomer() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val id = "2"
        val response = client.get("/customer/$id")
        assertEquals("Nenhum cliente com id $id", response.bodyAsText())
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}

class OrderRouteTests {
    @Test
    fun testGetOrder() = testApplication {
        val response = client.get("/order/2020-04-06-01")
        assertEquals(
            """{"number":"2020-04-06-01","contents":[{"item":"Ham Sandwich","amount":2,"price":5.5},{"item":"Water","amount":1,"price":1.5},{"item":"Beer","amount":3,"price":2.3},{"item":"Cheesecake","amount":1,"price":3.75}]}""",
            response.bodyAsText()
        )
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testGetNonExistentOrder() = testApplication {
        val response = client.get("/order/2020-04-06-02")
        assertEquals(
            "Pedido não encontrado",
            response.bodyAsText()
        )
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testTotatlizeOrder() = testApplication {
        val response = client.get("/order/2020-04-06-01/total")
        assertEquals(23.15, response.bodyAsText().toDouble())
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
