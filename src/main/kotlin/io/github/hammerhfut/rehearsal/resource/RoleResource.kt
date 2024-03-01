@file:Suppress("ktlint:standard:no-wildcard-imports")

package io.github.hammerhfut.rehearsal.resource

import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.model.db.*
import io.github.hammerhfut.rehearsal.model.dto.*
import io.github.hammerhfut.rehearsal.service.RoleService
import io.smallrye.common.annotation.RunOnVirtualThread
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import org.babyfish.jimmer.kt.new
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
) {
    @GET
    @RunOnVirtualThread
    fun getAllRoles(): Set<GetAllRolesResponseElement> {
        val roleSet =
            sqlClient.createQuery(Role::class) {
                select(
                    table.fetchBy {
                        allScalarFields()
                        roleGroup {
                            name()
                        }
                    },
                )
            }.execute()
        return roleService.sortRolesByGroup(roleSet)
    }

    @GET
    @Path("/{id}")
    @RunOnVirtualThread
    fun getOneRole(
        @RestPath id: Long,
    ): Role {
        val role =
            sqlClient.createQuery(Role::class) {
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
        val affectedRowCount =
            sqlClient.createUpdate(Role::class) {
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
    fun createRole(input: CreateRoleData) {
        sqlClient.save(
            new(Role::class).by {
                name = input.name
                remark = input.remark
                editable = true
                roleGroup().id = input.roleGroupId
                input.children.forEach {
                    children().addBy {
                        id = it
                    }
                }
            },
        )
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
    ): List<UserRoleBand> {
        return roleService.getRoleByUserId(id)
    }

    @PUT
    @Path("/user")
    @RunOnVirtualThread
    fun setUserRoles(input: SetUserRolesData) {
        sqlClient.save(roleService.parseRoleBandsToUser(input.userId, input.roles))
    }
}
