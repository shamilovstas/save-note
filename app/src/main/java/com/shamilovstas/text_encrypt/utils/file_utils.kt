package com.shamilovstas.text_encrypt.utils

import java.io.File


fun createUniqueFile(baseDir: File, filenameGenerator: () -> String): File {
    var file: File

    do {
        file = File(baseDir, filenameGenerator.invoke())
    } while (file.exists())
    return file
}
