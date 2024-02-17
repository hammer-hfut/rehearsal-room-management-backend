package io.github.hammerhfut.rehearsal.model.db

import org.babyfish.jimmer.sql.*

/**
 *@author prixii
 *@date 2024/2/17 19:26
 */

@Entity
interface UserRoleBand {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long

    @ManyToOne
    val role: Role

    @ManyToOne
    val user: User

    @ManyToOne
    val band: Band?
}
