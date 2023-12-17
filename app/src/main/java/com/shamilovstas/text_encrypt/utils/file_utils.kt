package com.shamilovstas.text_encrypt.utils

import java.io.File


fun createUniqueFile(baseDir: File, filename: String): File {
    var index = 1

    var uniqueFile = File(baseDir, filename)
    while (uniqueFile.exists()) {
        val name = "$filename($index)"
        uniqueFile = File(baseDir, name)
    }
    return uniqueFile
}
