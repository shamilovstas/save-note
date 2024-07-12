package com.shamilovstas.text_encrypt.utils

import java.net.URL

fun Any.loadFile(path: String): URL {
    return this.javaClass.classLoader!!.getResource(path)
}