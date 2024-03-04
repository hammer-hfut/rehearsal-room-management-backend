@file:Suppress("ktlint:standard:no-wildcard-imports")

package io.github.hammerhfut.rehearsal.service

import io.github.hammerhfut.rehearsal.model.db.*
import jakarta.inject.Singleton
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
