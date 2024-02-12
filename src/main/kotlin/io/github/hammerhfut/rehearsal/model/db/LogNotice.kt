package io.github.hammerhfut.rehearsal.model.db

import org.babyfish.jimmer.sql.*
import java.util.*

@Entity
interface LogNotice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: UUID
    val isRead: Boolean

    @Key
    @ManyToOne
    val log: OperationLog

    @Key
    @ManyToOne
    val receiver: User
}