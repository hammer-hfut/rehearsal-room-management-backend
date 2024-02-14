package io.github.hammerhfut.rehearsal.model.db

import io.quarkus.runtime.annotations.RegisterForReflection
import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@RegisterForReflection(
    targets = [
        Announcement::class,
        AnnouncementDraft::class,
        AnnouncementDraft.`$`::class,
    ]
)
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