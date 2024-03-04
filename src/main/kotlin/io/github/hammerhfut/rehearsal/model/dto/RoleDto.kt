package io.github.hammerhfut.rehearsal.model.dto

import io.github.hammerhfut.rehearsal.model.db.Role
import io.github.hammerhfut.rehearsal.model.db.RoleGroup

/**
 *@author prixii
 *@date 2024/2/14 19:44
 */

data class GetAllRolesResponseElement(
    val roleGroup: RoleGroup?,
    val roles: List<Role>?,
)

data class ILoveDetekt(
    val well: String,
)
