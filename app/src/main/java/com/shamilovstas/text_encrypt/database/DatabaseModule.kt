package com.shamilovstas.text_encrypt.database

import android.content.Context
import androidx.room.Room
import com.shamilovstas.text_encrypt.notes.repository.AttachmentStorageDao
import com.shamilovstas.text_encrypt.notes.repository.NotesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "app.db").build()
    }

    @Provides
    fun provideAttachmentStorageDao(appDatabase: AppDatabase): AttachmentStorageDao {
        return appDatabase.attachmentStorageDao()
    }

    @Provides
    fun provideNotesDao(appDatabase: AppDatabase): NotesDao {
        return appDatabase.notesDao()
    }
}