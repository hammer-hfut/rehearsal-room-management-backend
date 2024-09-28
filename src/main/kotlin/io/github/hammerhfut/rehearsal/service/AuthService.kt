@file:Suppress("ktlint:standard:no-wildcard-imports")

package io.github.hammerhfut.rehearsal.service

import io.github.hammerhfut.rehearsal.model.KeyCacheData
import io.github.hammerhfut.rehearsal.model.db.User
import io.github.hammerhfut.rehearsal.model.dto.RoleWithBandId
import io.github.hammerhfut.rehearsal.util.generateSecretKeySpec
import jakarta.inject.Singleton
import java.nio.ByteBuffer
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

/**
 *@author prixii
 *@date 2024/2/12 14:28
 */

val UTOKEN_LIFETIME = 30.days.toJavaDuration()
val KEY_LIFETIME = 1.hours.toJavaDuration()
const val RAND_KEY_RANGE = 100

@Singleton
class AuthService(
    private val userInfoCacheService: UserInfoCacheService,
    private val utokenCacheService: UtokenCacheService,
) {
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
                        ByteBuffer
                            .allocate(java.lang.Long.BYTES)
                            .putLong(id + serverTimestamp)
                            .array(),
                    )
            flag = utokenCacheService.isUtokenExist(utoken)
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
        userInfoCacheService.cacheUser(
            user,
            basicRoles,
        )
        utokenCacheService.cacheUtokenAndKey(
            utoken,
            user.id,
            KeyCacheData(key, keySpec),
        )
    }

    fun findUtokenCacheDataOrNull(utoken: String): KeyCacheData? = utokenCacheService.findKeyCacheData(utoken)

    fun refreshKey(
        key: Long,
        utoken: String,
    ): Int {
        val newKey: Long
        val rand = random.nextInt(1, RAND_KEY_RANGE)
        newKey =
            if (isOdd(key % rand)) {
                key + rand
            } else {
                key - rand
            }
        val newKeySpec = generateSecretKeySpec(newKey)
        utokenCacheService.cacheKey(
            utoken,
            KeyCacheData(
                newKey,
                newKeySpec,
            ),
        )
        return rand
    }

    private fun isOdd(num: Long) = num % 2 == 1L
}
