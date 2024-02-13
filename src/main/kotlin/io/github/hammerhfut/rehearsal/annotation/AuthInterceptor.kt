package io.github.hammerhfut.rehearsal.annotation

import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.service.AuthService
import io.github.hammerhfut.rehearsal.util.aesDecrypt
import io.vertx.ext.web.RoutingContext
import jakarta.annotation.Priority
import jakarta.interceptor.Interceptor
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.Provider

/**
 *@author prixii
 *@date 2024/2/12 19:52
 */

// 如果为 [true] 则跳过token和url认证
const val DEBUG_MODE = true

const val HEADER_UTOKEN = "x-rehearsal-utoken"
const val HEADER_TOKEN = "x-rehearsal-token"
const val LOGIN_API_PATH = "/auth/login"

@Priority(Interceptor.Priority.PLATFORM_BEFORE + 1)
@Provider
class AuthInterceptor(
    private val authService: AuthService
): ContainerRequestFilter {

    @Context
    var context: RoutingContext? = null

    @Context
    var header: HttpHeaders? = null

    @Context
    var uriInfo: UriInfo? = null

    override fun filter(p0: ContainerRequestContext?) {
        if (DEBUG_MODE) return

        val uToken = header?.getHeaderString(HEADER_UTOKEN)
        val encryptedUrl = header?.getHeaderString(HEADER_TOKEN)
        val path = uriInfo?.path
        if (path != LOGIN_API_PATH) {
            // 除了 [login] 都需要校验url合法性
            val badResponse = WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build())
            if (path == null || uToken == null || encryptedUrl == null) {
                throw badResponse
            }
             if (!checkUrl(uToken, encryptedUrl, path)) {
                 throw badResponse
             }
        }
    }

    private fun checkUrl(uToken: String, token: String, url: String): Boolean {
        val uTokenCache = authService.findUTokenCacheDataOrNull(uToken)
            ?: throw BusinessError(ErrorCode.NOT_FOUND)
        // TODO 检测是否过期
        val decryptedUrl = aesDecrypt(token, uTokenCache.keySpec)
        return  (decryptedUrl == url)
    }
}
