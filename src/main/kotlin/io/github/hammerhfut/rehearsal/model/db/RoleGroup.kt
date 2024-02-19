package io.github.hammerhfut.rehearsal.model.db

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id

/**
 *@author prixii
 *@date 2024/2/19 13:29
 */

@Entity
interface RoleGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long

    val name: String
}
