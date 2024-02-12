package io.github.hammerhfut.rehearsal.service

import jakarta.inject.Singleton
import org.mindrot.jbcrypt.BCrypt
import java.nio.ByteBuffer
import java.util.*

/**
 *@author prixii
 *@date 2024/2/12 14:28
 */

@Singleton
class AuthService {
    fun decodeUid(uid: String, timestamp: Long): Long {
        val byteArray = Base64.getDecoder().decode(uid)
        val username = byteArrayToLong(byteArray) - timestamp
        return username
    }


    private fun byteArrayToLong(byteArray: ByteArray): Long {

        var value: Long = 0

        for (i in byteArray.indices) {
            value = value shl 8
            value = value or (byteArray[i].toLong() and 0xff)
        }
        return value
    }

    private fun encodePwd(pwd: String) = BCrypt.hashpw(pwd, BCrypt.gensalt(12))

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
//            TODO 判定 [uToken] 是否已存在
            flag = false
        }
        val key = serverTimestamp - userTimestamp
//        TODO Caffeine缓存
        return Pair(uToken, serverTimestamp)
    }
}
