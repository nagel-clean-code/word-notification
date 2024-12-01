package com.nagel.wordnotification.utils

sealed class Toggles(val name: String) {
    object Purchases : Toggles("purchases")
}