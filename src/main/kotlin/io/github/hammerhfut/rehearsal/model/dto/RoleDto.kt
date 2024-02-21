package io.github.hammerhfut.rehearsal.model.dto

import io.github.hammerhfut.rehearsal.model.db.Role
import io.github.hammerhfut.rehearsal.model.db.RoleGroup
import io.github.hammerhfut.rehearsal.model.db.dto.GetAllRolesResponseElementDto

/**
 *@author prixii
 *@date 2024/2/14 19:44
 */

data class GetAllRolesResponseElement(
    val roleGroup: RoleGroup,
    val roles: List<GetAllRolesResponseElementDto>,
) {
    fun addRole(role: Role) =
        this.roles.addLast(
            GetAllRolesResponseElementDto(
                role,
            ),
        )
}

data class SetUserRoleData(
    val roles: List<RoleBand>,
    val userId: Long,
)

data class RoleBand(
    val roleId: Long,
    val bandId: Long?,
)

data class MoveToRoleGroupData(
    val roleId: Long,
    val roleGroupId: Long,
)

data class CreateRoleData(
    val name: String,
    val remark: String,
    val children: List<Long>,
    val roleGroupId: Long,
)
