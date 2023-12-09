package com.nagel.wordnotification.utils.common

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.nagel.wordnotification.R
import java.net.URLDecoder
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object StringUtils {
    const val rubSymbol: String = "&#x20bd"
    private const val RU_NUMBER_LENGTH = 11
    private const val RUSSIAN_PHONE = "79"
    private const val MASK_RGB = 0x00ffffff
    private const val MONETARY_SEPARATOR = '.'
    private const val PHONE_FORMAT = "%s (***) *** *%s %s"
    private const val RUB_FORMAT = "#,### ₽"
    private const val RUB_SEPARATOR = ' '
    private const val PHARMACY_PHONE_MASK = "XX XXX XXX XX XX"
    private const val DELIVERY_PHONE_FORMAT = "+X (XXX) XXX-XX-XX"
    private const val UTF8 = "utf-8"

    @JvmStatic
    fun getHtml(text: String?): Spanned {
        if (text == null) return Html.fromHtml("")
        val textResult = text.replace("<UL>", "")
            .replace("</UL>", "")
            .replace("<LI>", "• ")
            .replace("</LI>", "<br>")
        val htmlString: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(textResult, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(textResult)
        }
        return htmlString.trim() as Spanned
    }

    fun getSpanWithUrl(
        context: Context,
        text: String?,
        placeholder: String?,
        url: String?
    ): Spanned {
        return getSpanWithUrl(
            context = context,
            text = text,
            placeholder = placeholder,
            colorLink = null,
            isUnderline = false,
            url = url
        )
    }

    fun getSpanWithUrl(
        context: Context,
        text: String?,
        placeholder: String?,
        @ColorInt colorLink: Int?,
        isUnderline: Boolean,
        url: String?
    ): Spanned {
        return getSpanWithUrl(
            context = context,
            text = text,
            placeholder = placeholder,
            colorLink = colorLink,
            isUnderline = isUnderline
        ) {
            NavigationUtils.openLinkInBrowser(url, context)
        }
    }

    fun getSpanWithUrl(
        context: Context,
        text: String?,
        placeholder: String?,
        onClickListener: View.OnClickListener?
    ): Spanned {
        return getSpanWithUrl(
            context = context,
            text = text,
            placeholder = placeholder,
            colorLink = null,
            isUnderline = false,
            onClickListener
        )
    }

    fun getSpanWithUrl(
        context: Context,
        text: String?,
        placeholder: String?,
        @ColorInt colorLink: Int? = null,
        isUnderline: Boolean = false,
        onClickListener: View.OnClickListener?
    ): Spanned {
        if (text == null) return Html.fromHtml("")
        val res = SpannableString(text)
        if (!placeholder.isNullOrEmpty() && text.contains(placeholder)) {
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    onClickListener?.onClick(textView)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = colorLink ?: AndroidResourceUtils.getColor(context, R.color.green)
                    ds.isUnderlineText = isUnderline
                }
            }
            val start = text.indexOf(placeholder)
            val end = start + placeholder.length
            res.setSpan(clickableSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        return res
    }

    fun getSpanForPhone(
        context: Context,
        phoneNumber: String
    ): Spanned {
        val res = SpannableString(phoneNumber)
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) = Unit
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = AndroidResourceUtils.getColor(context, R.color.green)
                ds.isUnderlineText = false
            }
        }
        res.setSpan(clickableSpan, 0, phoneNumber.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return res
    }

    @JvmStatic
    fun formatPhoneNumber(phoneNumber: String?): String {
        if (phoneNumber == null || phoneNumber == "") {
            return ""
        }
        var number: String = phoneNumber
        try {
            if (!number.contains("+")) {
                number = "+$number"
            }
            if (RU_NUMBER_LENGTH < number.length) {
                val countryCode = number.substring(0, 2)
                val preLastNumber = number.substring(9, 10)
                val lastNumbers = number.substring(10)
                number = String.format(PHONE_FORMAT, countryCode, preLastNumber, lastNumbers)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return number
    }

    fun formatPharmacyPhoneNumber(phoneNumber: String): String {
        if (phoneNumber.isEmpty()) return phoneNumber

        val digit7 = '7'
        val digit8 = '8'
        val digit9 = '9'
        val plus = '+'
        var currentNumber = phoneNumber.replace(" ", "")
        when (currentNumber.first()) {
            digit7 -> currentNumber = plus + currentNumber
            digit8 -> {
                currentNumber = currentNumber.replaceFirst(digit8, digit7)
                currentNumber = plus + currentNumber
            }
            digit9 -> currentNumber = "$plus$digit7$currentNumber"
        }

        return currentNumber.applyMask(PHARMACY_PHONE_MASK)
    }

    fun formatDeliveryPhoneNumber(phoneNumber: String?): String {
        if (phoneNumber.isNullOrEmpty()) return ""

        return phoneNumber.applyMask(DELIVERY_PHONE_FORMAT)
    }

    fun isRussianPhone(phone: String): Boolean =
        phone.length < RUSSIAN_PHONE.length || phone.startsWith(RUSSIAN_PHONE)

    @JvmStatic
    fun formatMonetary(decimal: Float, pattern: String?): String {
        val otherSymbols = DecimalFormatSymbols(Locale.getDefault())
        otherSymbols.decimalSeparator = MONETARY_SEPARATOR
        otherSymbols.groupingSeparator = MONETARY_SEPARATOR
        val decimalFormat = DecimalFormat(pattern, otherSymbols)
        return decimalFormat.format(decimal.toDouble())
    }

    fun formatRub(decimal: Int): String? {
        val customSymbols = DecimalFormatSymbols.getInstance(Locale.getDefault())
        customSymbols.groupingSeparator = RUB_SEPARATOR
        val rubString = DecimalFormat(RUB_FORMAT, customSymbols).format(decimal.toLong())
        return rubString
    }

    fun getStringColor(@ColorRes resId: Int, context: Context): String {
        val argb = ContextCompat.getColor(context, resId)
        val rgb = argb and MASK_RGB
        val hex = Integer.toHexString(rgb)
        return context.getString(R.string.color_hex, hex)
    }

    fun removePunctuations(source: String): String {
        return source.replace("\\p{Punct}".toRegex(), "")
    }

    fun decodeLink(url: String?): String? =
        url?.let {
            URLDecoder.decode(it, UTF8)
        }
}