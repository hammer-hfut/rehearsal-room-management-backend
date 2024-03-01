package io.github.hammerhfut.rehearsal.model.dto

import io.github.hammerhfut.rehearsal.model.db.Role

/**
 *@author prixii
 *@date 2024/2/13 22:08
 */

data class LoginData(
    val username: String,
    val password: String,
    val timestamp: Long,
)

data class LoginResponse(
    val utoken: String,
    val lifetime: Long,
    val timestamp: Long,
    val user: LoginUserResponse,
)

data class LoginUserResponse(
    val realname: String,
    val basicRoles: List<RoleWithBandId>,
)

data class RoleWithBandId(
    val role: Role,
    val bandId: Long?,
)

data class RefreshKeyResponse(
    val lifetime: Long,
    val rand: Int,
)
