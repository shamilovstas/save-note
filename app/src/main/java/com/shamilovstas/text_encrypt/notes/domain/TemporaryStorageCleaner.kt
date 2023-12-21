package com.shamilovstas.text_encrypt.notes.domain

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.shamilovstas.text_encrypt.files.TemporaryFilesDir
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemporaryStorageCleaner @Inject constructor(
    @TemporaryFilesDir private val temporaryFilesDir: File
) {

    fun perform() {
        temporaryFilesDir.deleteRecursively()
    }
}


class CleanerLifecycleObserver @Inject constructor(private val cleaner: TemporaryStorageCleaner) :
    DefaultLifecycleObserver {

    fun init(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        cleaner.perform()
        super.onDestroy(owner)
    }
}
