package com.dungz.ads_open.model

import androidx.annotation.DrawableRes
import com.dungz.ads_open.R
import java.util.Locale

data class Language(
    val code: String,
    val name: String,
    val locale: Locale,
    @DrawableRes val flagRes: Int
)

object LanguageList {
    val languages = listOf(
        Language("en", "English", Locale.ENGLISH, R.drawable.flag_us),
        Language("pt_BR", "Brazil", Locale("pt", "BR"), R.drawable.flag_brazil),
        Language("es_VE", "Venezuela", Locale("es", "VE"), R.drawable.flag_venezuela),
        Language("ru", "Russia", Locale("ru"), R.drawable.flag_russia),
        Language("zh", "Chinese", Locale.CHINESE, R.drawable.flag_china),
        Language("vi", "Vietnamese", Locale("vi"), R.drawable.flag_vietnam),
        Language("th", "Thailand", Locale("th"), R.drawable.flag_thailand),
        Language("id", "Indonesia", Locale("id"), R.drawable.flag_indonesia),
        Language("de", "German", Locale.GERMAN, R.drawable.flag_germany),
        Language("pt", "Portugal", Locale("pt"), R.drawable.flag_portugal),
        Language("ko", "Korea", Locale.KOREAN, R.drawable.flag_korea),
        Language("ja", "Japanese", Locale.JAPANESE, R.drawable.flag_japan),
    )

    fun getByCode(code: String): Language? = languages.find { it.code == code }
}
