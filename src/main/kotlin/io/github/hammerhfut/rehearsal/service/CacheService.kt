package io.github.hammerhfut.rehearsal.service

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.hammerhfut.rehearsal.config.AppConfig
import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.model.UserInfoCache
import io.github.hammerhfut.rehearsal.model.db.Role
import io.github.hammerhfut.rehearsal.model.db.User
import io.github.hammerhfut.rehearsal.model.dto.RoleWithBandId
import io.github.hammerhfut.rehearsal.util.toBasicRoles
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.KSqlClient

/**
 *@author prixii
 *@date 2024/2/25 17:01
 */

@Singleton
class CacheService(
    private val appConfig: AppConfig,
    private val roleService: RoleService,
    private val sqlClient: KSqlClient,
) {
    private val utokenCache =
        Caffeine.newBuilder().maximumSize(appConfig.userCacheSize().data())
            .build<String, UserInfoCache>()

    private val userCache =
        Caffeine.newBuilder()
            .maximumSize(appConfig.userCacheSize().data())
            .build<Long, User> { userId -> writeUser(userId) }

    private val roleCache =
        Caffeine.newBuilder()
            .maximumSize(appConfig.userCacheSize().role())
            .build<Long, List<RoleWithBandId>> { key -> writeRoles(key) }

    fun cacheUser(
        utoken: String,
        userInfoCache: UserInfoCache,
        user: User,
        basicRoles: List<RoleWithBandId>,
    ) {
        utokenCache.put(utoken, userInfoCache)
        userCache.put(user.id, user)
        roleCache.put(user.id, basicRoles)
    }

    private fun writeUser(userId: Long): User {
        return sqlClient.findById(User::class, userId) ?: throw BusinessError(ErrorCode.NOT_FOUND)
    }

    fun invalidateUser(userId: Long) {
        userCache.invalidate(userId)
    }

    fun findUserByUtoken(utoken: String): User? {
        val userId = findUtokenCacheDataOrNull(utoken)?.userId ?: return null
        return userCache.get(userId)
    }

    fun findUserById(userId: Long): User? {
        return userCache.get(userId)
    }

    fun findUtokenCacheDataOrNull(utoken: String): UserInfoCache? = utokenCache.getIfPresent(utoken)

    private fun writeRoles(userId: Long): List<RoleWithBandId> {
        println("no cache try find role")
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

    fun invalidateUserRole(userId: Long) {
        roleCache.invalidate(userId)
    }
}
