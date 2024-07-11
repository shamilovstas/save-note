package com.shamilovstas.text_encrypt.utils

import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat


fun createActivityResultRegistry(result: Any?): ActivityResultRegistry {
    return object: ActivityResultRegistry() {
        override fun <I : Any?, O : Any?> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?
        ) {
            dispatchResult(requestCode, result)
        }
    }
}
