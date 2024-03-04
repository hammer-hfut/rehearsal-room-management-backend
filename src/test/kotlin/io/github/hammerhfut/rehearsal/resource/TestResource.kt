package io.github.hammerhfut.rehearsal.resource

import io.github.hammerhfut.rehearsal.interceptor.RolesRequired
import io.github.hammerhfut.rehearsal.model.BasicRoles
import io.github.hammerhfut.rehearsal.model.db.User
import io.github.hammerhfut.rehearsal.model.dto.RoleWithBandId
import io.github.hammerhfut.rehearsal.util.AuthUtil
import io.smallrye.common.annotation.RunOnVirtualThread
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import org.jboss.logging.Logger

/**
 *@author prixii
 *@date 2024/3/2 15:19
 */

@Path("/auth/test")
class TestResource(
    private val authUtil: AuthUtil,
) {
    private val logger = Logger.getLogger("AuthTest")

    @GET
    @Path("/token")
    @RunOnVirtualThread
    @RolesRequired(roles = [BasicRoles.APPOINTMENT, BasicRoles.EQUIPMENT], requireBand = true)
    fun testToken(
        @Context headers: HttpHeaders,
    ): String {
        val testMsg = "test token"
        logger.info(authUtil.getUser())
        logger.info(headers)
        return testMsg
    }

    @GET
    @Path("/user")
    @RunOnVirtualThread
    fun testUser(): User? {
        return authUtil.getUser()
    }

    @GET
    @Path("/role")
    @RunOnVirtualThread
    fun testRole(): List<RoleWithBandId>? {
        return authUtil.getBasicRoles()
    }
}
