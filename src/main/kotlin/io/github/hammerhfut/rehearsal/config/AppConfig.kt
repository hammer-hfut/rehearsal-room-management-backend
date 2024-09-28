package io.github.hammerhfut.rehearsal.config

import io.quarkus.runtime.annotations.StaticInitSafe
import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithDefault

/**
 *@author prixii
 *@date 2024/2/22 11:34
 */

@ConfigMapping(prefix = "app-config")
@StaticInitSafe
interface AppConfig {
    @WithDefault("false")
    fun debug(): Boolean

    @WithDefault("false")
    fun ignoreRole(): Boolean

    fun tokenlessApiPrefix(): List<String>

    fun headerAuth(): String

    fun userCacheSize(): UserCacheSize

    interface UserCacheSize {
        fun data(): Long

        fun role(): Long
    }
}
