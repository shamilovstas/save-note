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
    @CacheFilesDir
    fun provideCacheDir(@ApplicationContext context: Context): File {
        return context.cacheDir
    }

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }
}