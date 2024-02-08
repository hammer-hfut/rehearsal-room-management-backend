package io.github.hammerhfut.rehearsal.model.db

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
interface Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long
    val content: String
    val createTime: LocalDateTime
    val isPinned: Boolean

    @ManyToOne
    val author: User
}