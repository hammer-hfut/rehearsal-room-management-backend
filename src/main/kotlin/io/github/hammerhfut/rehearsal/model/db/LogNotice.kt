package io.github.hammerhfut.rehearsal.model.db

import org.babyfish.jimmer.sql.*

@Entity
interface LogNotice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long
    val isRead: Boolean

    @Key
    @ManyToOne
    val log: OperationLog

    @Key
    @ManyToOne
    val receiver: User
}