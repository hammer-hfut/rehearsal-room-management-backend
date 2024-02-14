package io.github.hammerhfut.rehearsal.model.db

import org.babyfish.jimmer.sql.*
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import java.time.LocalDateTime
import java.util.*

@Entity
interface OperationLog {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID
    val targetModuleName: ModuleName
    val dataBefore: String?
    val dataAfter: String?
    val createTime: LocalDateTime

    @OneToMany(mappedBy = "log")
    val notices: List<LogNotice>

    @ManyToOne
    val operator: User
}

@EnumType(EnumType.Strategy.NAME)
enum class ModuleName {
    ANNOUNCEMENT,
    APPOINTMENT,
    BAND,
    BAND_MEMBER,
    EQUIPMENT,
    PLACE,
    USER,
}