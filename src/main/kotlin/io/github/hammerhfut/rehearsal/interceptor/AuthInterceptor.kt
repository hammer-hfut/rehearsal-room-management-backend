package io.github.hammerhfut.rehearsal.interceptor

import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.service.AuthService
import io.github.hammerhfut.rehearsal.util.aesDecrypt
import io.github.hammerhfut.rehearsal.util.splitToken
import io.vertx.ext.web.RoutingContext
import jakarta.annotation.Priority
import jakarta.interceptor.Interceptor
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.Provider

/**
 *@author prixii
 *@date 2024/2/12 19:52
 */

// 如果为 [true] 则跳过token和url认证
const val DEBUG_MODE = true
const val LOGIN_API_PATH = "/auth/login"

@Priority(Interceptor.Priority.PLATFORM_BEFORE + 1)
@Provider
class AuthInterceptor(
    private val authService: AuthService,
    private val uriInfo: UriInfo,
    private val header: HttpHeaders,
): ContainerRequestFilter {

    override fun filter(p0: ContainerRequestContext?) {
        println("[auth-check]")
        if (DEBUG_MODE) return

        val token = header.getHeaderString(HEADER_AUTHORIZATION)
            ?: throw badResponse
        val (uToken, encryptedUrl) = splitToken(token)

        val path = uriInfo.path
        if (path != LOGIN_API_PATH && !checkUrl(uToken, encryptedUrl, path)) {
            // 除了 [login] 都需要校验url合法性
             throw badResponse
        }
    }

    private fun checkUrl(uToken: String, token: String, url: String): Boolean {
        val uTokenCache = authService.findUTokenCacheDataOrNull(uToken)
            ?: throw BusinessError(ErrorCode.NOT_FOUND)
        // TODO 检测是否过期
        val decryptedUrl = aesDecrypt(token, uTokenCache.keySpec)
        return  (decryptedUrl == url)
    }

    companion object {
        const val HEADER_AUTHORIZATION = "Authorization"
        val badResponse = BusinessError(ErrorCode.UNAUTHORIZED)
    }
}
