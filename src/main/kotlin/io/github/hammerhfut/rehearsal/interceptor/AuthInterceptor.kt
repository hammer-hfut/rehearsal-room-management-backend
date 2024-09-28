package io.github.hammerhfut.rehearsal.interceptor

import io.github.hammerhfut.rehearsal.config.AppConfig
import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.service.AuthService
import io.github.hammerhfut.rehearsal.service.UtokenCacheService
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
    private val utokenCacheService: UtokenCacheService,
    private val uriInfo: UriInfo,
    private val header: HttpHeaders,
    private val appConfig: AppConfig,
) : ContainerRequestFilter {
    override fun filter(context: ContainerRequestContext) {
        // debug 模式下不需要验证
        if (appConfig.debug()) return

        val path = uriInfo.path
        println("path: $path")
        if (!isTokenless(path)) {
            // 除了指定的 api 都需要校验 url 合法性
            val token =
                header.getHeaderString(appConfig.headerAuth())
                    ?: throw badResponse
            val (utoken, encryptedUrl) = splitToken(token)
            val result = checkUrl(utoken, encryptedUrl, path)
            when (result) {
                ErrorCode.SUCCEED -> return
                ErrorCode.NEED_REFRESH -> throw needRefreshResponse
                ErrorCode.UNAUTHORIZED -> throw badResponse
                else -> throw badResponse
            }
        }
    }

    private fun checkUrl(
        utoken: String,
        token: String,
        url: String,
    ): ErrorCode {
        if (!utokenCacheService.isUtokenExist(utoken)) return ErrorCode.UNAUTHORIZED
        val utokenCache =
            authService.findUtokenCacheDataOrNull(utoken) ?: return ErrorCode.NEED_REFRESH
        val decryptedUrl = aesDecrypt(token, utokenCache.keySpec)
        return if (decryptedUrl == url) ErrorCode.SUCCEED else ErrorCode.UNAUTHORIZED
    }

    private fun isTokenless(path: String): Boolean {
        appConfig.tokenlessApiPrefix().forEach { prefix ->
            if (path.startsWith(prefix)) return true
        }
        return false
    }

    companion object {
        private val badResponse = BusinessError(ErrorCode.UNAUTHORIZED)
        private val needRefreshResponse = BusinessError(ErrorCode.NEED_REFRESH)
    }
}
