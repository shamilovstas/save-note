package com.shamilovstas.text_encrypt.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shamilovstas.text_encrypt.database.converter.OffsetDateTimeConverter
import com.shamilovstas.text_encrypt.notes.repository.AttachmentStorageDao
import com.shamilovstas.text_encrypt.notes.repository.AttachmentEntity
import com.shamilovstas.text_encrypt.notes.repository.AttachmentStorageEntity
import com.shamilovstas.text_encrypt.notes.repository.NoteEntity
import com.shamilovstas.text_encrypt.notes.repository.NotesDao

@Database(
    entities = [
        NoteEntity::class,
        AttachmentEntity::class,
        AttachmentStorageEntity::class
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    version = 2
)
@TypeConverters(
    value = [
        OffsetDateTimeConverter::class
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notesDao(): NotesDao

    abstract fun attachmentStorageDao(): AttachmentStorageDao

}