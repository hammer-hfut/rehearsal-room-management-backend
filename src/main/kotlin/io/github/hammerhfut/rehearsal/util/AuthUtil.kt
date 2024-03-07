package io.github.hammerhfut.rehearsal.util

import io.github.hammerhfut.rehearsal.config.AppConfig
import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.model.db.User
import io.github.hammerhfut.rehearsal.model.dto.RoleWithBandId
import io.github.hammerhfut.rehearsal.service.CacheService
import jakarta.enterprise.context.RequestScoped
import jakarta.ws.rs.core.HttpHeaders

/**
 *@author prixii
 *@date 2024/3/2 11:33
 */

/**
 * 允许你在api中获取user的部分信息, 为了使用这个, 你需要在 Resource 中注入它， **而不是在api中**
 *
 * 它只能获取调用这个api的user
 *
 *  ```
 *  @Path("/auth/test")
 *  class TestResource(
 *     private val authUtil: AuthUtil,  // 注入AuthUtil
 *  ) {
 *     @GET
 *     @Path("/token")
 *     @RunOnVirtualThread
 *     fun test() {
 *         val user = authUtil.getUser()
 *     }
 *
 *  ```
 */
@RequestScoped
class AuthUtil(
    private val headers: HttpHeaders,
    private val appConfig: AppConfig,
    private val cacheService: CacheService,
) {
    fun getUser(): User? {
        val token = getUtoken()
        return cacheService.findUserByUtoken(token)
    }

    private fun getUserId(): Long {
        val utoken = getUtoken()
        return cacheService.findUserByUtoken(utoken)?.id ?: throw BusinessError(ErrorCode.NOT_FOUND)
    }

    private fun getUtoken(): String {
        return splitToken(getToken()).first
    }

    fun getToken(): String {
        return headers.getHeaderString(appConfig.headerAuth())
    }

    fun getBasicRoles(): List<RoleWithBandId>? {
        return cacheService.getRoleByUserId(getUserId())
    }
}
