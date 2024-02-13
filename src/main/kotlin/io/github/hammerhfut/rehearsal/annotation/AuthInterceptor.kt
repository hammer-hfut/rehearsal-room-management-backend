package io.github.hammerhfut.rehearsal.annotation

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

@Priority(Interceptor.Priority.PLATFORM_BEFORE + 1)
@Provider
class AuthInterceptor: ContainerRequestFilter {

    @Context
    var context: RoutingContext? = null

    @Context
    var header: HttpHeaders? = null

    @Context
    var uriInfo: UriInfo? = null

    override fun filter(p0: ContainerRequestContext?) {
        val uToken = header?.getHeaderString("uToken")
        val path = uriInfo?.path
        if (path != "/auth/login") {
            // 除了 [login] 都需要校验url合法性
            val badResponse = WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build())
            if (path == null || uToken == null) {
                throw badResponse
            }
            println("[url-checker!]")
             if (!checkUrl(uToken, path)) {
                 throw badResponse
             }
        }
    }

    private fun checkUrl(uToken: String, url: String): Boolean {
        // TODO [url] 校验
        println("[uToken]: $uToken, [url] $url:")
        return true
    }
}
