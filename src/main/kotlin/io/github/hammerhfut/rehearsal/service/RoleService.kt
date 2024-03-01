@file:Suppress("ktlint:standard:no-wildcard-imports")

package io.github.hammerhfut.rehearsal.service

import io.github.hammerhfut.rehearsal.model.db.*
import io.github.hammerhfut.rehearsal.model.db.dto.GetAllRolesResponseElementDto
import io.github.hammerhfut.rehearsal.model.dto.GetAllRolesResponseElement
import io.github.hammerhfut.rehearsal.model.dto.RoleBand
import jakarta.inject.Singleton
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq

/**
 *@author prixii
 *@date 2024/2/14 9:40
 */

@Singleton
class RoleService(
    private val sqlClient: KSqlClient,
) {
    fun sortRolesByGroup(rolesWithGroup: List<Role>): Set<GetAllRolesResponseElement> {
        val sortedRoles = mutableSetOf<GetAllRolesResponseElement>()
        rolesWithGroup.forEach {
            var found = false
            for (sortedRole in sortedRoles) {
                if (sortedRole.roleGroup.id == it.roleGroup?.id) {
                    found = true
                    sortedRole.addRole(it)
                    break
                }
            }
            if (!found) {
                it.roleGroup?.let { it1 ->
                    GetAllRolesResponseElement(
                        it1,
                        mutableListOf(GetAllRolesResponseElementDto(it)),
                    )
                }?.let { it2 ->
                    sortedRoles.add(
                        it2,
                    )
                }
            }
        }
        return sortedRoles
    }

    fun parseRoleBandsToUser(
        userId: Long,
        roleBands: List<RoleBand>,
    ): User {
        return new(User::class).by {
            id = userId
            roleBands.forEach {
                userRoleBands().addBy {
                    user().id = userId
                    role().id = it.roleId
                    band = null
                    it.bandId?.let { band().id = it }
                }
            }
        }
    }

    fun getRoleByUserId(id: Long): List<UserRoleBand> {
        return sqlClient.createQuery(UserRoleBand::class) {
            where(table.userId eq id)
            select(
                table.fetchBy {
                    allScalarFields()
                    role {
                        allScalarFields()
                        `children*`()
                        roleGroup {
                            name()
                        }
                    }
                    band {
                        allScalarFields()
                    }
                },
            )
        }.execute()
    }
}
