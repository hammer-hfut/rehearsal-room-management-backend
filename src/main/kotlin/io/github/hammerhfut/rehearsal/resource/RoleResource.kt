package io.github.hammerhfut.rehearsal.resource

import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.model.db.*
import io.github.hammerhfut.rehearsal.model.dto.GetAllRolesResponseElement
import io.github.hammerhfut.rehearsal.service.sortRolesByGroup
import io.smallrye.common.annotation.RunOnVirtualThread
import jakarta.ws.rs.GET
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
) {
    @GET
    @RunOnVirtualThread
    fun getAllRoles(): List<GetAllRolesResponseElement>  {
        val list = sqlClient.createQuery(Role::class) {
            select(
                table.fetchBy {
                    allScalarFields()
                    roleGroup {
                        name()
                    }
                }
            )
        }.execute()
        return sortRolesByGroup(list)
    }

    @GET
    @Path("/{id}")
    @RunOnVirtualThread
    fun getOneRole(@RestPath id: Long): Role {
        val role = sqlClient.createQuery(Role::class) {
            where(table.id.eq(id))
            select(
                table.fetchBy {
                    allScalarFields()
                    `children*`()
                    roleGroup()
                }
            )
        }.fetchOneOrNull()
            ?:throw BusinessError(ErrorCode.NOT_FOUND)
        return role
    }

    @GET
    @Path("/user/{id}")
    fun getRoleByUserId(@RestPath id: Long): List<UserRoleBand> {
        val roles = sqlClient.createQuery(UserRoleBand::class){
            where(table.userId eq id)
            select(
                table.fetchBy {
                    allScalarFields()
                    role{
                        allScalarFields()
                        `children*`()
                        roleGroup {
                            name()
                        }
                    }
                    band {
                        allScalarFields()
                    }
                }
            )
        }.execute()
        return roles
    }
}
