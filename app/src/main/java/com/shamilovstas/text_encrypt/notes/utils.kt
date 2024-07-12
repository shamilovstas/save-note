package com.shamilovstas.text_encrypt.notes

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val MEDIUM_DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM)
val SHORT_DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)