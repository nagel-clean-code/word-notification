package com.nagel.wordnotification.data.accounts.entities

data class Account(
    var id: Long,
    val userName: String,
    val idAuthorUUID: String
)