package io.github.hammerhfut.rehearsal.model.dto

/**
 *@author prixii
 *@date 2024/2/13 22:08
 */

data class LoginData(
    val username: String,
    val password: String,
    val timestamp: Long
)

data class LoginResponse(
    val utoken: String,
    val lifetime: Long,
    val timestamp: Long
)

data class RefreshKeyResponse (
    val lifetime: Long,
    val rand: Int
)
