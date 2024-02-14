package io.github.hammerhfut.rehearsal.model.db

import io.quarkus.runtime.annotations.RegisterForReflection
import org.babyfish.jimmer.sql.*

@RegisterForReflection(
    targets = [
        Place::class,
        PlaceDraft::class,
        PlaceDraft.`$`::class,
    ]
)
@Entity
interface Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    val name: String
}