package io.github.hammerhfut.rehearsal.config

import io.quarkus.runtime.annotations.StaticInitSafe
import io.smallrye.config.ConfigMapping

/**
 *@author prixii
 *@date 2024/2/22 11:34
 */

@ConfigMapping(prefix = "app-config")
@StaticInitSafe
interface AppConfig {
    fun debug(): Boolean

    fun loginApiPath(): String
}
