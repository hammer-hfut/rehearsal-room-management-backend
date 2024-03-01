package io.github.hammerhfut.rehearsal.model.db

import org.babyfish.jimmer.sql.*

/**
 *@author prixii
 *@date 2024/2/19 13:29
 */

@Entity
interface RoleGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long

    @Key
    val name: String
}
