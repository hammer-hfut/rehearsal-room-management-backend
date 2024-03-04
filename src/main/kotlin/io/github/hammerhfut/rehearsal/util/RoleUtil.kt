package io.github.hammerhfut.rehearsal.util

import io.github.hammerhfut.rehearsal.model.db.Role
import io.github.hammerhfut.rehearsal.model.db.UserRoleBand
import io.github.hammerhfut.rehearsal.model.dto.RoleWithBandId
import io.github.hammerhfut.rehearsal.service.CacheService
import jakarta.inject.Singleton

/**
 *@author prixii
 *@date 2024/2/14 18:55
 */

@Singleton
class RoleUtil(
    private val cacheService: CacheService,
) {
    fun getRoleByUserId(userId: Long): List<RoleWithBandId>? {
        return cacheService.getRoleByUserId(userId)
    }
}

fun UserRoleBand.toBasicRoles(): Set<Pair<Role, Long?>> {
    val basicRoles = mutableSetOf<Pair<Role, Long?>>()
    val bandId = this.band?.id
    val role = this.role
    if (role.children.isNotEmpty()) {
        role.children.forEach {
            basicRoles.addAll(it.toBasicRoles(bandId))
        }
    } else {
        basicRoles.add(Pair(role, bandId))
    }
    return basicRoles
}

fun Role.toBasicRoles(bandId: Long?): Set<Pair<Role, Long?>> {
    val basicRoles = mutableSetOf<Pair<Role, Long?>>()
    if (this.children.isNotEmpty()) {
        this.children.forEach {
            basicRoles.addAll(it.toBasicRoles(bandId))
        }
    } else {
        basicRoles.add(Pair(this, bandId))
    }
    return basicRoles
}
