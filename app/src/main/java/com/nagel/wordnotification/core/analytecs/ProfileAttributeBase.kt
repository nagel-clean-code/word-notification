package com.nagel.wordnotification.core.analytecs

sealed class ProfileAttributeBase(val name: String, val value: Any) {

    class GoogleServicesAttributes(value: Any) : ProfileAttributeBase(
        name = "google_services",
        value = value
    )

    class WordCounterAttributes(value: Any) : ProfileAttributeBase(
        name = "word_counter",
        value = value
    )
}