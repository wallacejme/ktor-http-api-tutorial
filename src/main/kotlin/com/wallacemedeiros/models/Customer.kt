package com.wallacemedeiros.models

import kotlinx.serialization.Serializable

@Serializable
data class Customer(val id: String, val firstName: String, val lastName: String, val email: String)
data class Customer(val id: Int, val firstName: String, val lastName: String, val email: String)

val customerStorage = mutableListOf<Customer>()