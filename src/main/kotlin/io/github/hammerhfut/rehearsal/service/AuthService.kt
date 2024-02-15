package io.github.hammerhfut.rehearsal.service

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.hammerhfut.rehearsal.model.UTokenCacheData
import io.github.hammerhfut.rehearsal.util.generateSecretKeySpec
import jakarta.inject.Singleton
import java.nio.ByteBuffer
import java.security.Key
import java.util.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

/**
 *@author prixii
 *@date 2024/2/12 14:28
 */

val LIFETIME = 1.hours.toJavaDuration()
const val MAX_NUM: Long = 0xff
const val BYTE_SIZE = 8

@Singleton
class AuthService {
    private val uTokenCache = Caffeine.newBuilder()
        .expireAfterWrite(LIFETIME)
        .build<String, UTokenCacheData>()

    private val random = Random()

    fun decodeUid(uid: String, timestamp: Long): Long {
        val byteArray = Base64.getDecoder().decode(uid)
        val username = byteArrayToLong(byteArray) - timestamp
        return username
    }

    private fun byteArrayToLong(byteArray: ByteArray): Long {

        var value: Long = 0
        for (i in byteArray.indices) {
            value = value shl BYTE_SIZE
            value = value or (byteArray[i].toLong() and MAX_NUM)
        }
        return value
    }

    /**
     * @return (uToken, timestamp)
     */
    fun generateUToken(id: Long, userTimestamp: Long):Pair<String, Long> {
        var flag = true
        var uToken = ""
        var serverTimestamp: Long = 0
        while (flag) {
            serverTimestamp = System.currentTimeMillis()
            uToken = Base64
                .getEncoder()
                .encodeToString(
                    ByteBuffer.allocate(java.lang.Long.BYTES)
                        .putLong(id + serverTimestamp)
                        .array())
            flag =  uTokenCache.getIfPresent(uToken) != null
        }
        val key = serverTimestamp + userTimestamp
        val keySpec = generateSecretKeySpec(key.toString())
        uTokenCache.put(uToken, UTokenCacheData(id, LIFETIME.toMillis(), key, keySpec))
        return Pair(uToken, serverTimestamp)
    }

    fun findUTokenCacheDataOrNull(uToken: String): UTokenCacheData? = uTokenCache.getIfPresent(uToken)

    fun refreshKey(uTokenCacheData: UTokenCacheData): Int {
        var key = uTokenCacheData.key
        val rand = random.nextInt(100)
        if (isOdd(key % rand % 2)) {
            key += rand
        } else {
            key -= rand
        }
        uTokenCacheData.key = key
        uTokenCacheData.keySpec = generateSecretKeySpec(key.toString())
        return rand
    }

    private fun isOdd(num: Long) = num % 2 == 1L
}
