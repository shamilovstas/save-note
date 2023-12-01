package com.shamilovstas.text_encrypt.notes.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update

@Dao
interface NotesDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(entity: NoteEntity)

    @Insert(onConflict = REPLACE)
    fun insert(entities: List<NoteEntity>)

    @Query("SELECT id, content, is_published, created_date FROM notes")
    fun getAllNotes(): List<NoteEntity>

    @Delete
    fun delete(entity: NoteEntity)

    @Update
    fun update(entity: NoteEntity)
}