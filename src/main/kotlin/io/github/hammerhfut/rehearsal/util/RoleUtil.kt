package io.github.hammerhfut.rehearsal.util


/**
 *@author prixii
 *@date 2024/2/14 18:55
 */

/**
 * [roleName]: 角色，可以携带命名空间
 * @return: (band, role)
 */
fun splitRole(roleName: String): Pair<String, String> {
    val band = roleName.substringBefore(':')
    val role = roleName.substringAfter(':')
    return Pair(band, role)
}
