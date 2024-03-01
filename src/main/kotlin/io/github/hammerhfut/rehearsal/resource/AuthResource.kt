package io.github.hammerhfut.rehearsal.resource

import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.interceptor.AuthInterceptor
import io.github.hammerhfut.rehearsal.interceptor.RolesRequired
import io.github.hammerhfut.rehearsal.model.BasicRoles
import io.github.hammerhfut.rehearsal.model.db.User
import io.github.hammerhfut.rehearsal.model.db.fetchBy
import io.github.hammerhfut.rehearsal.model.db.username
import io.github.hammerhfut.rehearsal.model.dto.*
import io.github.hammerhfut.rehearsal.service.AuthService
import io.github.hammerhfut.rehearsal.service.LIFETIME
import io.github.hammerhfut.rehearsal.util.RoleUtil
import io.github.hammerhfut.rehearsal.util.splitToken
import io.smallrye.common.annotation.RunOnVirtualThread
import jakarta.ws.rs.GET
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.jboss.logging.Logger
import org.jboss.resteasy.reactive.RestPath
import org.mindrot.jbcrypt.BCrypt

/**
 *@author prixii
 *@date 2024/2/12 13:10
 */

@Path("/auth")
class AuthResource(
    private val sqlClient: KSqlClient,
    private val authService: AuthService,
    private val roleUtil: RoleUtil,
) {
    private val logger = Logger.getLogger("Auth")

    @POST
    @Path("/login")
    @RunOnVirtualThread
    fun login(input: LoginData): LoginResponse {
        val user =
            sqlClient.createQuery(User::class) {
                where(table.username.eq(input.username))
                select(
                    table.fetchBy {
                        allScalarFields()
                    },
                )
            }.fetchOneOrNull()
                ?.takeIf { BCrypt.checkpw(input.password, it.password) }
                ?: throw BusinessError(ErrorCode.FORBIDDEN) // TODO 异常处理
        val (utoken, timestamp) = authService.generateUtoken(user.id, input.timestamp)
        logger.info("[utoken]: $utoken")
        val basicRoles = roleUtil.getRoleByUserId(user.id) ?: listOf()
        return LoginResponse(
            utoken = utoken,
            lifetime = LIFETIME.toMillis(),
            timestamp = timestamp,
            user =
                LoginUserResponse(
                    realname = user.realname,
                    roles = listOf(),
                    basicRoles = basicRoles,
                ),
        )
    }

    @GET
    @Path("/test-token")
    @RunOnVirtualThread
    @RolesRequired(roles = [BasicRoles.APPOINTMENT, BasicRoles.EQUIPMENT], requireBand = true)
    fun testToken(test: Test): String {
        val testMsg = "test token"
        return testMsg
    }

    data class Test(
        val str: String,
        val bandId: Long,
    )

    @PUT
    @Path("/refresh/{key}")
    @RunOnVirtualThread
    fun refreshKey(
        @RestPath key: Long,
        @HeaderParam(AuthInterceptor.HEADER_AUTHORIZATION) token: String,
    ): RefreshKeyResponse {
        val utokenCache =
            authService.findUtokenCacheDataOrNull(splitToken(token).first)
                ?.takeIf { it.key == key }
                ?: throw BusinessError(ErrorCode.NOT_FOUND)
        return RefreshKeyResponse(
            rand = authService.refreshKey(utokenCache),
            lifetime = LIFETIME.toMillis(),
        )
    }
}
