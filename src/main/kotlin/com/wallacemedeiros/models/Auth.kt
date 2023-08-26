package com.wallacemedeiros.models

import kotlinx.serialization.Serializable

@Serializable
data class User(val username: String, val passwordHash: String)

@Serializable
data class PostUser(val username: String, val password: String)