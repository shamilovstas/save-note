package com.shamilovstas.text_encrypt.files

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File

@Module
@InstallIn(SingletonComponent::class)
class FilesModule {

    @Provides
    @InternalFilesDir
    fun provideBaseDir(@ApplicationContext context: Context): File {
        return context.filesDir
    }

    @Provides
    @TemporaryFilesDir
    fun provideTemporaryFilesDir(@ApplicationContext context: Context): File {

        val cacheDir = context.cacheDir

        val filename = "notemediatempdir"
        val dir = File(cacheDir, filename)
        return dir
    }

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }
}