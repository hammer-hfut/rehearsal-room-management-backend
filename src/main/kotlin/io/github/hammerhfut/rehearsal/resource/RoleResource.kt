package io.github.hammerhfut.rehearsal.resource

import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.model.db.*
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
    fun getAllRoles(): List<Role> {
        val list = sqlClient.createQuery(Role::class) {
            select(
                table.fetchBy {
                    allScalarFields()
                    `children*`{
                        depth(0)
                    }
                }
            )
        }.execute()
        return list
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
                    upperRole()
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
