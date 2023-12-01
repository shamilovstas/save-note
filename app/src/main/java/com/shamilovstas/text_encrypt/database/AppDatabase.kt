package com.shamilovstas.text_encrypt.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shamilovstas.text_encrypt.database.converter.OffsetDateTimeConverter
import com.shamilovstas.text_encrypt.notes.repository.NoteEntity
import com.shamilovstas.text_encrypt.notes.repository.NotesDao

@Database(
    entities = [
        NoteEntity::class
    ], version = 1
)
@TypeConverters(value = [
    OffsetDateTimeConverter::class
])
abstract class AppDatabase : RoomDatabase() {

    abstract fun notesDao(): NotesDao

}