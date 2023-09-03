package com.wallacemedeiros

import com.wallacemedeiros.models.Customer
import com.wallacemedeiros.models.PostCustomer
import com.wallacemedeiros.models.PostUser
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.*

class CustomerRouteTest {
    private val luizGonzaga = Customer(1, "Luiz", "Gonzaga", "luiz.gonzaga@musica.com")
    @Test
    fun testCreateCustomer() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val customerCountBeforePost = client.get("/customer").body<List<Customer>>().size
        val response = client.post("/customer") {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("firstName", luizGonzaga.firstName)
                put("lastName", luizGonzaga.lastName)
                put("email", luizGonzaga.email)
            })
        }
        val responseBody = response.body<Customer>()
        assertEquals(luizGonzaga.firstName, responseBody.firstName)
        assertEquals(luizGonzaga.lastName, responseBody.lastName)
        assertEquals(luizGonzaga.email, responseBody.email)
        assertEquals(HttpStatusCode.Created, response.status)

        val customerCountAfterPost = client.get("/customer").body<List<Customer>>().size
        assertEquals(customerCountBeforePost + 1, customerCountAfterPost)
    }

    @Test
    fun testGetCustomer() = testApplication {
        val customerCreationClient = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val createdCustomer = customerCreationClient.post("/customer") {
            contentType(ContentType.Application.Json)
            setBody(PostCustomer(luizGonzaga.firstName, luizGonzaga.lastName, luizGonzaga.email))
        }.body<Customer>()
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val response = client.get("/customer/${createdCustomer.id}")
        assertEquals(createdCustomer, response.body())
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testGetNonExistentCustomer() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val id = "0"
        val response = client.get("/customer/$id")
        assertEquals("Nenhum cliente com id $id", response.bodyAsText())
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testEditCustomer() = testApplication {
        val customClient = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        var customerId = customClient.get("/customer").body<List<Customer>>().lastOrNull()?.id
        if (customerId == null) {
            customerId = customClient.post("/customer") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("firstName", luizGonzaga.firstName)
                    put("lastName", luizGonzaga.lastName)
                    put("email", luizGonzaga.email)
                })
            }.body<Customer>().id
        }

        val newCustomerData = PostCustomer("Zeca", "Baleiro", "baleiro@musica.com")
        val putResponse = customClient.put("/customer/$customerId") {
            contentType(ContentType.Application.Json)
            setBody(newCustomerData)
        }
        assertTrue(putResponse.bodyAsText().isEmpty())
        assertEquals(HttpStatusCode.NoContent, putResponse.status)

        val updatedCustomer = customClient.get("/customer/$customerId")
        assertEquals(
            Customer(customerId, newCustomerData.firstName, newCustomerData.lastName, newCustomerData.email),
            updatedCustomer.body()
        )
        assertEquals(HttpStatusCode.OK, updatedCustomer.status)
    }

    @Test
    fun testDeleteCustomer() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val allCustomers = client.get("/customer").body<List<Customer>>()
        var customerCountBeforeDelete = allCustomers.size
        var customerId = allCustomers.lastOrNull()?.id
        if (customerCountBeforeDelete == 0) {
            customerId = client.post("/customer") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("firstName", luizGonzaga.firstName)
                    put("lastName", luizGonzaga.lastName)
                    put("email", luizGonzaga.email)
                })
            }.body<Customer>().id
            customerCountBeforeDelete++
        }
        val deleteResponse = client.delete("/customer/$customerId")
        assertTrue(deleteResponse.bodyAsText().isEmpty())
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        val customerCountAfterDelete = client.get("/customer").body<List<Customer>>().size
        assertEquals(customerCountBeforeDelete - 1, customerCountAfterDelete)
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
