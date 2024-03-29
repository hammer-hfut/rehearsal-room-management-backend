@file:Suppress("ktlint:standard:no-wildcard-imports")

package io.github.hammerhfut.rehearsal.service

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.hammerhfut.rehearsal.model.UserInfoCache
import io.github.hammerhfut.rehearsal.model.db.User
import io.github.hammerhfut.rehearsal.model.dto.RoleWithBandId
import io.github.hammerhfut.rehearsal.util.generateSecretKeySpec
import jakarta.inject.Singleton
import java.nio.ByteBuffer
import java.util.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

/**
 *@author prixii
 *@date 2024/2/12 14:28
 */

val LIFETIME = 1.hours.toJavaDuration()
const val RAND_KEY_RANGE = 100

@Singleton
class AuthService(
    private val cacheService: CacheService,
) {
    private val utokenCache =
        Caffeine.newBuilder()
            .expireAfterWrite(LIFETIME)
            .build<String, UserInfoCache>()

    private val random = Random()

    /**
     * @return (utoken, timestamp)
     */
    fun generateUtoken(id: Long): Pair<String, Long> {
        var flag = true
        var utoken = ""
        var serverTimestamp: Long = 0
        while (flag) {
            serverTimestamp = System.currentTimeMillis()
            utoken =
                Base64
                    .getEncoder()
                    .encodeToString(
                        ByteBuffer.allocate(java.lang.Long.BYTES)
                            .putLong(id + serverTimestamp)
                            .array(),
                    )
            flag = utokenCache.getIfPresent(utoken) != null
        }

        return Pair(utoken, serverTimestamp)
    }

    fun cacheUserInfo(
        serverTimestamp: Long,
        userTimestamp: Long,
        utoken: String,
        user: User,
        basicRoles: List<RoleWithBandId>,
    ) {
        val key = serverTimestamp + userTimestamp
        val keySpec = generateSecretKeySpec(key)
        cacheService.cacheUser(
            utoken,
            UserInfoCache(user.id, LIFETIME.toMillis(), key, keySpec),
            user,
            basicRoles,
        )
    }

    fun findUtokenCacheDataOrNull(utoken: String): UserInfoCache? = cacheService.findUtokenCacheDataOrNull(utoken)

    fun refreshKey(userInfoCache: UserInfoCache): Int {
        var key = userInfoCache.key
        val rand = random.nextInt(1, RAND_KEY_RANGE)
        if (isOdd(key % rand)) {
            key += rand
        } else {
            key -= rand
        }
        userInfoCache.key = key
        userInfoCache.keySpec = generateSecretKeySpec(key)
        return rand
    }

    private fun isOdd(num: Long) = num % 2 == 1L
}
