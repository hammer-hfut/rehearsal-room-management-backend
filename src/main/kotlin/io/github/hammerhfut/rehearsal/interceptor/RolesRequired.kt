@file:Suppress("ktlint:standard:no-wildcard-imports")

package io.github.hammerhfut.rehearsal.interceptor

import io.github.hammerhfut.rehearsal.config.AppConfig
import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.model.BasicRoles
import io.github.hammerhfut.rehearsal.model.db.Band
import io.github.hammerhfut.rehearsal.model.db.Role
import io.github.hammerhfut.rehearsal.model.db.UserRoleBand
import io.github.hammerhfut.rehearsal.service.RoleService
import io.github.hammerhfut.rehearsal.service.UtokenCacheService
import io.github.hammerhfut.rehearsal.util.splitToken
import jakarta.annotation.Priority
import jakarta.enterprise.util.Nonbinding
import jakarta.interceptor.AroundInvoke
import jakarta.interceptor.Interceptor
import jakarta.interceptor.InterceptorBinding
import jakarta.interceptor.InvocationContext
import jakarta.ws.rs.core.HttpHeaders
import org.jboss.logging.Logger
import java.lang.annotation.Inherited
import java.util.*
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.jvm.kotlinFunction

/**
 * [roles] BasicRoles的list, 若用户拥有这些role中的任何一个，就可以访问该接口
 *
 * [requireBand] 为 `true` 时, 用户需要拥有对应 band 的权限才能访问该接口
 *
 * **e.g.**: `@RolesRequired([BasicRoles.HRM, BasicRoles.APPOINTMENT])`, 用户只要拥有 hrm 或 appoint 中的任何一个就可以访问该接口
 *
 * 如果需要使用 [requireBand] ，务必通过json object携带bandId, 并将bandId放在根上
 *
 * ```
 * // GOOD
 * "data": {
 *   "bandId": 123
 * }
 *
 * // BAD
 * "data": 123
 * ```
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@InterceptorBinding
@Inherited
annotation class RolesRequired(
    @get:Nonbinding val roles: Array<BasicRoles>,
    @get:Nonbinding val requireBand: Boolean = false,
)

@Interceptor
@RolesRequired(roles = [BasicRoles.HRM, BasicRoles.BAND])
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 1)
@Suppress("MISSING_DEPENDENCY_CLASS")
class RolesRequiredInterceptor(
    private val header: HttpHeaders,
    private val roleService: RoleService,
    private val appConfig: AppConfig,
    private val utokenCacheService: UtokenCacheService,
) {
    private val logger = Logger.getLogger("RolesRequiredLogger")

    @AroundInvoke
    fun intercept(context: InvocationContext): Any? {
        if (appConfig.ignoreRole()) return context.proceed()
        val token = header.getHeaderString(appConfig.headerAuth())
        val (utoken, _) = splitToken(token)
        val (rolesRequired, requireBand) = getAnnotationInfo(context)

        val targetBandId: Long? =
            if (requireBand) {
                getBandId(context)
            } else {
                null
            }
        // TODO 缓存获取 role
        checkRole(getRolesByUtoken(utoken), rolesRequired, targetBandId).let {
            if (!it) {
                throw BusinessError(ErrorCode.NOT_FOUND)
            }
        }
        return context.proceed()
    }

    private fun getAnnotationInfo(context: InvocationContext): Pair<Array<String>, Boolean> {
        val annotation =
            context.method.kotlinFunction
                ?.findAnnotations(RolesRequired::class)
                ?.get(0)
                ?: throw BusinessError(ErrorCode.NOT_FOUND)
        val stringRoles = mutableListOf<String>()
        annotation.roles.forEach {
            stringRoles.addLast(it.toString().lowercase(Locale.getDefault()))
        }
        return Pair(stringRoles.toTypedArray<String>(), annotation.requireBand)
    }

    private fun getRolesByUtoken(utoken: String): List<UserRoleBand> {
        val id = utokenCacheService.findUserId(utoken) ?: throw BusinessError(ErrorCode.UNAUTHORIZED)
        return roleService.getRoleByUserId(id)
    }

    private fun checkRole(
        roleMappings: List<UserRoleBand>,
        targetRolesName: Array<String>,
        targetBandId: Long?,
    ): Boolean {
        for (roleMapping in roleMappings) {
            if (checkChild(roleMapping.role, targetRolesName)) {
                logger.info("find it!")
                if (checkBand(roleMapping.band, targetBandId)) {
                    logger.info("also does band!")
                    return true
                }
            }
        }
        return false
    }

    private fun checkChild(
        role: Role,
        targetRolesName: Array<String>,
    ): Boolean {
        if (role.children.isEmpty()) {
            return (targetRolesName.contains(role.name))
        }
        var result = false
        for (child in role.children) {
            if (checkChild(child, targetRolesName)) {
                result = true
                break
            }
        }
        return result
    }

    private fun getBandId(context: InvocationContext): Long? {
        val bandId =
            context.parameters[0]
                .javaClass.declaredFields
                .firstOrNull { it.name == "bandId" }
        bandId?.isAccessible = true
        return bandId?.get(context.parameters[0]) as Long?
    }

    private fun checkBand(
        band: Band?,
        targetBandId: Long?,
    ): Boolean {
        if (targetBandId == null) return true
        // 有 targetBandId 说明需要判定乐队
        if (band == null) return false
        return (band.id == targetBandId)
    }
}
