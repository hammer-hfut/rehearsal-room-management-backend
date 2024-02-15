package io.github.hammerhfut.rehearsal.util

import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 *@author prixii
 *@date 2024/2/13 21:22
 */

const val AES_KEY_LENGTH = 16
const val U_TOKEN_LENGTH = 12
const val BEARER_LENGTH = 7
const val CIPHER_INSTANCE_NAME = "AES/ECB/PKCS5Padding"
const val CRYPT_ALGORITHM = "AES"
const val FILL_CHARACTER = 'X'

fun aesDecrypt(encryptedText: String, secretKeySpec: SecretKeySpec): String {
    val cipher = Cipher.getInstance(CIPHER_INSTANCE_NAME)
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
    val decodedBytes = Base64.getDecoder().decode(encryptedText)
    val decryptedBytes = cipher.doFinal(decodedBytes)
    return String(decryptedBytes)
}

fun aesEncrypt(rawUrl: String, keySpec: SecretKeySpec): String {
    val cipher = Cipher.getInstance(CIPHER_INSTANCE_NAME)
    cipher.init(Cipher.ENCRYPT_MODE, keySpec)
    val encryptedBytes = cipher.doFinal(rawUrl.toByteArray(StandardCharsets.UTF_8))
    return Base64.getEncoder().encodeToString(encryptedBytes)
}

/**
 * [key]: Long直接 toString 就行了
 */
fun generateSecretKeySpec(key: String): SecretKeySpec {
    val aesKey = key.padStart(AES_KEY_LENGTH, FILL_CHARACTER)
    return SecretKeySpec(aesKey.toByteArray(), CRYPT_ALGORITHM)
}

/**
 * @return (utoken, urlToken)
 */
fun splitToken(token: String): Pair<String, String> {
    var utoken: String
    var urlToken: String
    token.substring(BEARER_LENGTH).also {
        utoken = it.substring(0, U_TOKEN_LENGTH)
        urlToken = it.substring(U_TOKEN_LENGTH)
    }
    return Pair(utoken, urlToken)
}
