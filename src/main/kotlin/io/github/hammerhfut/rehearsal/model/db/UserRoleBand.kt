package io.github.hammerhfut.rehearsal.model.db

import org.babyfish.jimmer.sql.*

/**
 *@author prixii
 *@date 2024/2/17 19:26
 */

@Entity
interface UserRoleBand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    val user: User

    @Key
    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    val role: Role

    @Key
    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    val band: Band?
}
