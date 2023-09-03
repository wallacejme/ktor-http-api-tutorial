package com.wallacemedeiros.dao

import com.wallacemedeiros.models.Customer

interface CustomerDAOFacade {
    suspend fun all(): List<Customer>
    suspend fun getOne(id: Int): Customer?
    suspend fun addNew(firstName: String, lastName: String, email: String): Customer?
    suspend fun edit(id: Int, firstName: String, lastName: String, email: String): Boolean
    suspend fun delete(id: Int): Boolean
}