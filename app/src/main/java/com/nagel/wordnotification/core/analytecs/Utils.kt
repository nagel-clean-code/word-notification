package com.nagel.wordnotification.core.analytecs

import io.appmetrica.analytics.profile.Attribute
import io.appmetrica.analytics.profile.UserProfileUpdate
import java.math.BigDecimal

fun createProfileAttribute(data: ProfileAttributeBase): UserProfileUpdate<*> = with(data) {
    return when (value) {
        is String -> {
            Attribute.customString(name).withValue(value)
        }

        is Double -> {
            Attribute.customNumber(name).withValue(value)
        }

        is Float -> {
            val number = BigDecimal(value.toString()).toDouble()
            Attribute.customNumber(name).withValue(number)
        }

        is Int -> {
            val number = BigDecimal(value).toDouble()
            Attribute.customNumber(name).withValue(number)
        }

        is Boolean -> {
            Attribute.customBoolean(name).withValue(value)
        }

        else -> {
            Attribute.customString(name).withValue(value.toString())
        }
    }
}