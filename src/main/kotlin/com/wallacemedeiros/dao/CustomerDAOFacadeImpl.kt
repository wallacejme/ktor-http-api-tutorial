package com.wallacemedeiros.dao

import com.wallacemedeiros.dao.DatabaseFactory.dbQuery
import com.wallacemedeiros.models.Customer
import com.wallacemedeiros.models.Customers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class CustomerDAOFacadeImpl : CustomerDAOFacade {
    private fun resultRowToCustomer(row: ResultRow) = Customer(
        id = row[Customers.id],
        firstName = row[Customers.firstName],
        lastName = row[Customers.lastName],
        email = row[Customers.email],
    )

    override suspend fun all(): List<Customer> = dbQuery {
        Customers.selectAll().map(::resultRowToCustomer)
    }

    override suspend fun getOne(id: Int): Customer? = dbQuery {
        Customers
            .select { Customers.id eq id }
            .map(::resultRowToCustomer)
            .singleOrNull()
    }

    override suspend fun addNew(firstName: String, lastName: String, email: String): Customer? = dbQuery {
        val insertStatement = Customers.insert {
            it[Customers.firstName] = firstName
            it[Customers.lastName] = lastName
            it[Customers.email] = email
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToCustomer)
    }

    override suspend fun edit(id: Int, firstName: String, lastName: String, email: String): Boolean = dbQuery {
        Customers.update({ Customers.id eq id }) {
            it[Customers.firstName] = firstName
            it[Customers.lastName] = lastName
            it[Customers.email] = email
        } > 0
    }

    override suspend fun delete(id: Int): Boolean = dbQuery {
        Customers.deleteWhere{ Customers.id eq id } > 0
    }
}

val customerDAO: CustomerDAOFacade = CustomerDAOFacadeImpl().apply {
    runBlocking {
        if(all().isEmpty()) {
            addNew("Test", "Customer", "test.customer@email.com")
        }
    }
}