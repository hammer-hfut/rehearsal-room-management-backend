@file:Suppress("ktlint:standard:no-wildcard-imports")

package io.github.hammerhfut.rehearsal.resource

import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.model.db.*
import io.github.hammerhfut.rehearsal.model.db.dto.CreateRoleData
import io.github.hammerhfut.rehearsal.model.db.dto.MoveToRoleGroupData
import io.github.hammerhfut.rehearsal.model.db.dto.SetUserRolesData
import io.github.hammerhfut.rehearsal.model.dto.*
import io.github.hammerhfut.rehearsal.service.RoleService
import io.github.hammerhfut.rehearsal.service.UserInfoCacheService
import io.smallrye.common.annotation.RunOnVirtualThread
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.jboss.resteasy.reactive.RestPath

/**
 *@author prixii
 *@date 2024/2/14 9:40
 */

@Path("/role")
class RoleResource(
    private val sqlClient: KSqlClient,
    private val roleService: RoleService,
    private val userInfoCacheService: UserInfoCacheService,
) {
    @GET
    @RunOnVirtualThread
    fun getAllRoles(): Set<GetAllRolesResponseElement> {
        // BUG 不知道为啥每次登录后，会多出一个奇怪的 roleGroup，详见 /roles 页面
        val roles =
            sqlClient
                .createQuery(Role::class) {
                    select(
                        table.fetchBy {
                            allScalarFields()
                            roleGroup {
                                name()
                            }
                        },
                    )
                }.execute()
        val roleGroupMap = roles.groupBy { it.roleGroup }
        val groupedRoles = mutableSetOf<GetAllRolesResponseElement>()
        roleGroupMap.forEach { (k, v) -> groupedRoles.add(GetAllRolesResponseElement(k, v)) }
        return groupedRoles
    }

    @GET
    @Path("/{id}")
    @RunOnVirtualThread
    fun getOneRole(
        @RestPath id: Long,
    ): Role {
        val role =
            sqlClient
                .createQuery(Role::class) {
                    where(table.id.eq(id))
                    select(
                        table.fetchBy {
                            allScalarFields()
                            `children*`()
                            roleGroup()
                        },
                    )
                }.fetchOneOrNull()
                ?: throw BusinessError(ErrorCode.NOT_FOUND)
        return role
    }

    @PUT
    @Path("/role-group")
    @RunOnVirtualThread
    fun moveToRoleGroup(input: MoveToRoleGroupData) {
//        TODO
        val affectedRowCount =
            sqlClient
                .createUpdate(Role::class) {
                    where(table.id eq input.roleId)
                    set(
                        table.roleGroupId,
                        input.roleGroupId,
                    )
                }.execute()
        if (affectedRowCount == 0) throw BusinessError(ErrorCode.FORBIDDEN)
    }

    @POST
    @RunOnVirtualThread
    fun createRole(input: CreateRoleData): Long {
//        TODO
        val id =
            sqlClient
                .save(
                    input.toEntity().copy {
                        editable = true
                    },
                ).modifiedEntity.id
        return id
    }

    @DELETE
    @Path("/{id}")
    @RunOnVirtualThread
    fun deleteRole(
        @RestPath id: Long,
    ) {
        sqlClient.deleteById(Role::class, id)
    }

    @GET
    @Path("/user/{id}")
    fun getRoleByUserId(
        @RestPath id: Long,
    ): List<UserRoleBand> = roleService.getRoleByUserId(id)

    @PUT
    @Path("/user")
    @RunOnVirtualThread
    fun setUserRoles(input: SetUserRolesData) {
        sqlClient.save(input.toEntity())
        userInfoCacheService.invalidateUserRole(input.userId)
    }
}
