package io.github.hammerhfut.rehearsal.service

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.hammerhfut.rehearsal.config.AppConfig
import io.github.hammerhfut.rehearsal.model.KeyCacheData
import jakarta.inject.Singleton

/**
 *@author prixii
 *@date 2024/9/28 10:26
 */

@Singleton
class UtokenCacheService(
    private val appConfig: AppConfig,
) {
    /**
     * 存储 utoken 到 userId 的映射
     * utoken: refresh token
     * <utoken, userId>
     */
    private val utokenCache =
        Caffeine
            .newBuilder()
            .expireAfterWrite(UTOKEN_LIFETIME)
            .build<String, Long>()

    /**
     * 存储 utoken 到 keyCacheData 的映射
     * key: access token
     * <utoken, keyCacheData>
     */
    private val keyCache =
        Caffeine
            .newBuilder()
            .maximumSize(appConfig.userCacheSize().data())
            .expireAfterWrite(KEY_LIFETIME)
            .build<String, KeyCacheData>()

    fun cacheUtokenAndKey(
        utoken: String,
        userId: Long,
        keyCacheData: KeyCacheData,
    ) {
        utokenCache.put(utoken, userId)
        keyCache.put(utoken, keyCacheData)
    }

    fun cacheKey(
        utoken: String,
        keyCacheData: KeyCacheData,
    ) {
        keyCache.put(utoken, keyCacheData)
    }

    fun isUtokenExist(utoken: String): Boolean = utokenCache.getIfPresent(utoken) != null

    fun findUserId(utoken: String): Long? = utokenCache.getIfPresent(utoken)

    fun findKeyCacheData(utoken: String): KeyCacheData? = keyCache.getIfPresent(utoken)
}
