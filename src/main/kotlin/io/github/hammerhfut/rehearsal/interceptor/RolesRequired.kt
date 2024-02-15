package io.github.hammerhfut.rehearsal.interceptor

import io.github.hammerhfut.rehearsal.util.splitToken
import jakarta.annotation.Priority
import jakarta.interceptor.AroundInvoke
import jakarta.interceptor.Interceptor
import jakarta.interceptor.InterceptorBinding
import jakarta.interceptor.InvocationContext
import jakarta.ws.rs.core.HttpHeaders

/**
 *@author prixii
 *@date 2024/2/14 9:20
 */

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@InterceptorBinding
annotation class RolesRequired (val rolesRequired: Array<String> )

@RolesRequired(rolesRequired = ["admin.annotation"])
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 1)
class RolesRequiredInterceptor(
    private val header: HttpHeaders
) {

    @AroundInvoke
    fun execute(context: InvocationContext): Any? {
        val token = header.getHeaderString(AuthInterceptor.HEADER_AUTHORIZATION)
            ?: throw AuthInterceptor.badResponse
        val (uToken, _) = splitToken(token)
        println("[uToken]: $uToken")
        return context.proceed()
    }
}
