package io.github.hammerhfut.rehearsal.model

import javax.crypto.spec.SecretKeySpec

/**
 *@author prixii
 *@date 2024/2/13 20:20
 */

data class UTokenCacheData(
    val id: Long,
    val lifetime: Long,
    var key: Long,
    var keySpec: SecretKeySpec
)
