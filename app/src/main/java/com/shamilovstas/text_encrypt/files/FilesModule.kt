package com.shamilovstas.text_encrypt.files

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
}