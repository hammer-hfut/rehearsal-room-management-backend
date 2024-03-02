package io.github.hammerhfut.rehearsal.resource

import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.model.db.RoleGroup
import io.github.hammerhfut.rehearsal.model.db.fetchBy
import io.github.hammerhfut.rehearsal.model.db.id
import io.github.hammerhfut.rehearsal.model.db.name
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
 *@date 2024/2/20 13:29
 */

@Path("/role/group")
class RoleGroupResource(
    private val sqlClient: KSqlClient,
) {
    @POST
    @RunOnVirtualThread
    fun createRoleGroup(input: RoleGroup) {
        sqlClient.save(input)
    }

    @GET
    @RunOnVirtualThread
    fun getRoleGroups(): List<RoleGroup> {
        return sqlClient.createQuery(RoleGroup::class) {
            select(
                table.fetchBy {
                    allScalarFields()
                },
            )
        }.execute()
    }

    @PUT
    @RunOnVirtualThread
    fun renameRoleGroup(input: RoleGroup) {
        val affectedRowCount =
            sqlClient.createUpdate(RoleGroup::class) {
                set(
                    table.name,
                    input.name,
                )
                where(table.id eq input.id)
            }.execute()
        if (affectedRowCount == 0) {
            throw BusinessError(ErrorCode.NOT_FOUND)
        }
    }

    @DELETE
    @Path("/{id}")
    @RunOnVirtualThread
    fun removeRoleGroup(
        @RestPath id: Long,
    ) {
        val affectedRowCount =
            sqlClient.createDelete(RoleGroup::class) {
                where(table.id eq id)
            }.execute()
//        TODO 检测数量 删除
        if (affectedRowCount == 0) {
            throw BusinessError(ErrorCode.NOT_FOUND)
        }
    }
}
