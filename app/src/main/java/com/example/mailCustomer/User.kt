package com.example.mailCustomer

data class User(
    val userId: Int,
    val username: String,
    val email: String,
    var status: String,
    var role: String
)
