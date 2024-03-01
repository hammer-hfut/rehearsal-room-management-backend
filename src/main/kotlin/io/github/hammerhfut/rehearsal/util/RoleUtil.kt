package io.github.hammerhfut.rehearsal.util

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.hammerhfut.rehearsal.config.AppConfig
import io.github.hammerhfut.rehearsal.model.db.Role
import io.github.hammerhfut.rehearsal.model.db.UserRoleBand
import io.github.hammerhfut.rehearsal.model.dto.RoleWithBandId
import io.github.hammerhfut.rehearsal.service.RoleService
import jakarta.inject.Singleton

/**
 *@author prixii
 *@date 2024/2/14 18:55
 */

@Singleton
class RoleUtil(
    private val roleService: RoleService,
    appConfig: AppConfig,
) {
    private val roleCache =
        Caffeine.newBuilder()
            .maximumSize(appConfig.userCacheSize().data())
            .build<Long, List<RoleWithBandId>> { key -> writeRoles(key) }

    private fun writeRoles(userId: Long): List<RoleWithBandId> {
        val basicRoleSet = mutableSetOf<Pair<Role, Long?>>()
        roleService.getRoleByUserId(userId).forEach {
            basicRoleSet.addAll(it.toBasicRoles())
        }
        val basicRoles = mutableListOf<RoleWithBandId>()
        basicRoleSet.forEach {
            basicRoles.addLast(RoleWithBandId(it.first, it.second))
        }
        return basicRoles
    }

    fun getRoleByUserId(userId: Long): List<RoleWithBandId>? {
        return roleCache.get(userId)
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
