package io.github.hammerhfut.rehearsal.interceptor

import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.model.db.Role
import io.github.hammerhfut.rehearsal.model.db.UserRoleBand
import io.github.hammerhfut.rehearsal.resource.AuthResource
import io.github.hammerhfut.rehearsal.service.AuthService
import io.github.hammerhfut.rehearsal.service.RoleService
import io.github.hammerhfut.rehearsal.util.splitToken
import jakarta.annotation.Priority
import jakarta.enterprise.util.Nonbinding
import jakarta.interceptor.AroundInvoke
import jakarta.interceptor.Interceptor
import jakarta.interceptor.InterceptorBinding
import jakarta.interceptor.InvocationContext
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.UriInfo
import org.jboss.logging.Logger
import java.lang.annotation.Inherited
import java.lang.reflect.Method

/**
 * [roles] role的name的list, 若用户拥有这些role中的任何一个，就可以访问该接口
 *
 * [requireBand] 为 true 时, 用户需要拥有对应 band 的权限才能访问该接口
 *
 * e.g.: @RolesRequired(["hrm", "appoint"]), 用户只要拥有 hrm 或 appoint 中的任何一个就可以访问该接口
 *
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@InterceptorBinding
@Inherited
annotation class RolesRequired(
    @get:Nonbinding val roles: Array<String>,
    @get:Nonbinding val requireBand: Boolean = false,
)

@Interceptor
@RolesRequired(roles = ["role1", "role2"])
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 1)
@Suppress("MISSING_DEPENDENCY_CLASS")
class RolesRequiredInterceptor(
    private val header: HttpHeaders,
    private val authService: AuthService,
    private val roleService: RoleService,
    private val apiInfo: UriInfo,
) {
    private val logger = Logger.getLogger("RolesRequiredLogger")

    @AroundInvoke
    fun intercept(context: InvocationContext): Any? {
        val token = header.getHeaderString(AuthInterceptor.HEADER_AUTHORIZATION)
        val (utoken, _) = splitToken(token)
        val path: String = apiInfo.path
        val annotation =
            findMethodByRoutePath(path)?.getAnnotation(RolesRequired::class.java)
                ?: return BusinessError(ErrorCode.NOT_FOUND)
        val rolesRequired = annotation.roles
        logger.info("[band]: ${annotation.requireBand}")
        val targetBandId = 0L
        // TODO 缓存获取 role
        checkRole(getRolesByUtoken(utoken), rolesRequired, targetBandId)
        return context.proceed()
    }

    private fun findMethodByRoutePath(path: String): Method? {
        val resourceClass = AuthResource::class.java
        val pathPrefix = resourceClass.getAnnotation(Path::class.java).value
        val methods = resourceClass.methods
        for (method in methods) {
            val route = method.getAnnotation(Path::class.java)
            if (route != null && "$pathPrefix${route.value}" == path) {
                return method
            }
        }
        return null
    }

    private fun getRolesByUtoken(utoken: String): List<UserRoleBand> {
        val id = authService.findUtokenCacheDataOrNull(utoken)?.id ?: throw BusinessError(ErrorCode.UNAUTHORIZED)
        return roleService.getRoleByUserId(id)
    }

    private fun checkRole(
        roleMappings: List<UserRoleBand>,
        targetRolesName: Array<String>,
        targetBandId: Long?,
    ): Boolean {
        for (roleMapping in roleMappings) {
            for (child in roleMapping.role.children) {
                if (checkChild(child, targetRolesName)) {
                    logger.info("find it!")
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
        logger.info("[check]: $role")
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
}
