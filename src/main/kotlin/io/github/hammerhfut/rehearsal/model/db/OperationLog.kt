package io.github.hammerhfut.rehearsal.model.db

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime
import java.util.*

@Entity
interface OperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: UUID
    val targetModuleName: String
    val dataBefore: String
    val dataAfter: String
    val createTime: LocalDateTime

    @OneToMany(mappedBy = "log")
    val notices: List<LogNotice>

    @ManyToOne
    val operator: User
}