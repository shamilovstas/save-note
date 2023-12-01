package com.shamilovstas.text_encrypt.database

import android.content.Context
import androidx.room.Room
import com.shamilovstas.text_encrypt.notes.repository.NotesDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    fun provideDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "app.db").build()
    }

    @Provides
    fun bindNotesDao(appDatabase: AppDatabase): NotesDao {
        return appDatabase.notesDao()
    }
}