package io.github.hammerhfut.rehearsal.interceptor

import io.github.hammerhfut.rehearsal.config.AppConfig
import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.service.AuthService
import io.github.hammerhfut.rehearsal.util.aesDecrypt
import io.github.hammerhfut.rehearsal.util.splitToken
import jakarta.annotation.Priority
import jakarta.interceptor.Interceptor
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.Provider

/**
 *@author prixii
 *@date 2024/2/12 19:52
 */

@Priority(Interceptor.Priority.PLATFORM_BEFORE + 1)
@Provider
class AuthInterceptor(
    private val authService: AuthService,
    private val uriInfo: UriInfo,
    private val header: HttpHeaders,
    private val appConfig: AppConfig,
) : ContainerRequestFilter {
    override fun filter(context: ContainerRequestContext) {
        // debug 模式下不需要验证
        if (appConfig.debug()) return

        val path = uriInfo.path
        if (path != appConfig.loginApiPath()) {
            // 除了 [login] 都需要校验url合法性
            val token =
                header.getHeaderString(appConfig.headerAuth())
                    ?: throw badResponse
            val (utoken, encryptedUrl) = splitToken(token)
            if (!checkUrl(utoken, encryptedUrl, path)) {
                throw badResponse
            }
        }
    }

    private fun checkUrl(
        utoken: String,
        token: String,
        url: String,
    ): Boolean {
        val utokenCache =
            authService.findUtokenCacheDataOrNull(utoken)
                ?: throw BusinessError(ErrorCode.NOT_FOUND)
        // TODO 检测是否过期
        val decryptedUrl = aesDecrypt(token, utokenCache.keySpec)
        return (decryptedUrl == url)
    }

    companion object {
        private val badResponse = BusinessError(ErrorCode.UNAUTHORIZED)
    }
}
