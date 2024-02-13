package io.github.hammerhfut.rehearsal.util

import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 *@author prixii
 *@date 2024/2/13 21:22
 */

const val AES_KEY_LENGTH = 16
const val CIPHER_INSTANCE_NAME = "AES/ECB/PKCS5Padding"
const val CRYPT_ALGORITHM = "ARE"
const val FILL_CHARACTER = 'X'

fun aesDecrypt(encryptedText: String, secretKeySpec: SecretKeySpec): String {
    val cipher = Cipher.getInstance(CIPHER_INSTANCE_NAME)
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
    val decodedBytes = Base64.getDecoder().decode(encryptedText)
    val decryptedBytes = cipher.doFinal(decodedBytes)
    return String(decryptedBytes)
}

fun generateSecretKeySpec(key: String): SecretKeySpec {
    val aesKey = key.padStart(AES_KEY_LENGTH, FILL_CHARACTER)
    return SecretKeySpec(aesKey.toByteArray(), CRYPT_ALGORITHM)

}
