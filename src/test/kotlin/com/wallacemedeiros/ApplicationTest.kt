package com.wallacemedeiros

import com.wallacemedeiros.models.Customer
import com.wallacemedeiros.models.PostUser
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
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
        val customerCreationClient = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        customerCreationClient.post("/customer") {
            contentType(ContentType.Application.Json)
            setBody(luizGonzaga)
        }
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

class AuthRouteTests {
    private val validUser = PostUser("test_user", "password")
    @Test
    fun testSuccessfulLogin() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(validUser)
        }
        val bodyAsMap = Json.decodeFromString<Map<String, String>>(response.bodyAsText())
        assertContains(bodyAsMap, "token")
        assertNotEquals("", bodyAsMap.getOrDefault("token", ""))
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testFailingLogin() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(PostUser("invalid_user", "invalid_password"))
        }
        assertEquals("Usuário inválido", response.bodyAsText())
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testUnauthorizedAccess() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val response = client.get("/hello-jwt") {
            headers["Authorization"] = "Bearer invalid_token"
        }
        assertEquals("Token expirou ou é inválido", response.bodyAsText())
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testAuthorizedAccess() = testApplication {
        val loginClient = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val loginResponse = loginClient.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(validUser)
        }
        val token = Json.decodeFromString<Map<String, String>>(loginResponse.bodyAsText())["token"]

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val response = client.get("/hello-jwt") {
            headers["Authorization"] = "Bearer $token"
        }
        assertContains(response.bodyAsText(), "Hello, test_user!")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
