package io.github.hammerhfut.rehearsal.service

import io.github.hammerhfut.rehearsal.model.db.Role
import io.github.hammerhfut.rehearsal.model.db.dto.GetAllRolesResponseElementDto
import io.github.hammerhfut.rehearsal.model.dto.GetAllRolesResponseElement

/**
 *@author prixii
 *@date 2024/2/14 9:40
 */

fun sortRolesByGroup(rolesWithGroup: List<Role>): List<GetAllRolesResponseElement> {
    val sortedRoles = mutableListOf<GetAllRolesResponseElement>()
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
            sortedRoles.addLast(it.roleGroup?.let { it1 ->
                GetAllRolesResponseElement(
                    it1,
                    mutableListOf(GetAllRolesResponseElementDto(it))
                )
            })
        }
    }
    return sortedRoles
}
