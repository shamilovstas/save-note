package com.shamilovstas.text_encrypt

import android.util.Log
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


private const val TAG = "TextEncryptor"

@Singleton
class TextEncryptor @Inject constructor() {

    companion object {
        private const val SALT_LENGTH = 20
        private const val IV_LENGTH = 16
        private const val ALGORITHM = "AES/CBC/PKCS5Padding"
        private const val MIN_BLOCK_SIZE = 16
        private const val DIGEST_ALGO = "PBKDF2WithHmacSHA256"

    }

    @OptIn(ExperimentalEncodingApi::class)
    fun encrypt(text: String, password: String): String {
        if (text.isEmpty()) throw IllegalArgumentException("Clear text cannot be empty")
        val salt = generateRandom(SALT_LENGTH)
        val key = generateKey(password, salt)

        val cipher = Cipher.getInstance(ALGORITHM)

        val iv = IvParameterSpec(generateRandom(IV_LENGTH))
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val ciphertext: ByteArray = cipher.doFinal(text.toByteArray())

        val message = salt + iv.iv + ciphertext

        Log.d("TextEncryptor","Before:  ${message.toString(Charsets.UTF_8)}")
        val base64Encoded = Base64.encode(message)
        Log.d("TextEncryptor","After:  $base64Encoded")
        return base64Encoded
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun decrypt(encText: String, password: String): String {
        val encText2 = Base64.decode(encText)
        checkMessageSize(encText2.size)

        val cipher = Cipher.getInstance(ALGORITHM)

        val salt = encText2.slice(0 until SALT_LENGTH).toByteArray()
        var curIndex: Int = SALT_LENGTH
        val ivBytes = encText2.slice(curIndex until curIndex + IV_LENGTH).toByteArray()
        curIndex += IV_LENGTH
        val key = generateKey(password, salt)
        val iv = IvParameterSpec(ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, key, iv)

        val message = encText2.slice(curIndex until encText2.size).toByteArray()
        val ciphertext: ByteArray = cipher.doFinal(message)
        return ciphertext.toString(Charsets.UTF_8)
    }

    private fun checkMessageSize(size: Int) {
        val minSize = MIN_BLOCK_SIZE + SALT_LENGTH + IV_LENGTH
        if (size < minSize) throw EncryptedMessageMalformed(size, minSize)
    }

    private fun generateKey(
        password: String,
        salt: ByteArray
    ): SecretKeySpec {
        val keyFactory = SecretKeyFactory.getInstance(DIGEST_ALGO);
        val keySpec = PBEKeySpec(password.toCharArray(), salt, 1, 128)

        val secret = keyFactory.generateSecret(keySpec)
        val key = SecretKeySpec(secret.encoded, "AES")
        return key
    }

    private fun generateRandom(len: Int): ByteArray {
        val random = SecureRandom()
        val rnd = ByteArray(len)
        random.nextBytes(rnd)
        return rnd
    }
}

class EncryptedMessageMalformed(messageLength: Int, minLength: Int) :
    Exception("The encrypted message was malformed, message length $messageLength < $minLength")