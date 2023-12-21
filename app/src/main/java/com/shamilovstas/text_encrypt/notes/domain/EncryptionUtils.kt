package com.shamilovstas.text_encrypt.notes.domain

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionUtils @Inject constructor() {
    companion object {
        const val ALGORITHM = "AES/CBC/PKCS5Padding"
        const val ALGORITHM2 = "AES/CBC/NoPadding"
        const val MIN_BLOCK_SIZE = 16
        const val DIGEST_ALGO = "PBKDF2WithHmacSHA256"
    }

    val cipher = Cipher.getInstance(ALGORITHM)

    fun generateKey(
        password: String,
        salt: ByteArray
    ): SecretKeySpec {
        val keyFactory = SecretKeyFactory.getInstance(DIGEST_ALGO);
        val keySpec = PBEKeySpec(password.toCharArray(), salt, 1, 128)

        val secret = keyFactory.generateSecret(keySpec)
        val key = SecretKeySpec(secret.encoded, "AES")
        return key
    }

    fun generateRandom(len: Int): ByteArray {
        val random = SecureRandom()
        val rnd = ByteArray(len)
        random.nextBytes(rnd)
        return rnd
    }

    fun generateIv(size: Int): IvParameterSpec {
        return IvParameterSpec(generateRandom(size))
    }

    fun encryptBytes(data: ByteArray, iv: IvParameterSpec, key: SecretKeySpec): ByteArray {
        if (data.isEmpty()) throw IllegalArgumentException("Clear text cannot be empty")
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val cipherdata: ByteArray = cipher.doFinal(data)
        return cipherdata
    }
}

class EncryptedMessageMalformed(messageLength: Int, minLength: Int) :
    Exception("The encrypted message was malformed, message length $messageLength < $minLength")