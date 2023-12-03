package com.shamilovstas.text_encrypt.export

import com.shamilovstas.text_encrypt.notes.domain.Note
import java.io.File
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.format.DateTimeFormatter

class ExportInteractor {

    private val baseDir = File("/asd/asd/asd")

    companion object {
        private val FILENAME_FROM_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyymmdd_hhmmss")
    }
    fun export(note: Note) {

        val textFile = createNewTextContentFile(note)
        writeTextData(textFile, note)
    }

    private fun writeTextData(textFile: Path, note: Note) {
        val serializedDate = DateTimeFormatter.ISO_DATE_TIME.format(note.createdDate)
        val lines = listOf<String>(
            serializedDate, note.content
        )
        Files.write(textFile, lines, StandardOpenOption.WRITE)
    }

    private fun createNewTextContentFile(note: Note): Path {
        val createdDate = requireNotNull(note.createdDate)
        val name = FILENAME_FROM_DATE_FORMATTER.format(createdDate)
        val filename = createNameForFile(name)
        val baseDirPath = baseDir.toPath()

        val filepath = baseDirPath.resolve(filename)
        return Files.createFile(filepath)
    }

    private fun createNameForFile(name: String): String {
        val extension = ".encn"
        return name + extension
    }
}