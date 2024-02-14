package io.github.hammerhfut.rehearsal.model.db

import org.babyfish.jimmer.sql.*
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import java.util.*

@Entity
interface LogNotice {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID
    val isRead: Boolean

    @Key
    @ManyToOne
    val log: OperationLog

    @Key
    @ManyToOne
    val receiver: User
}