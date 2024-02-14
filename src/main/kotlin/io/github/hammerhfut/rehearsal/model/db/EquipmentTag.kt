package io.github.hammerhfut.rehearsal.model.db

import io.quarkus.runtime.annotations.RegisterForReflection
import org.babyfish.jimmer.sql.*

@RegisterForReflection(
    targets = [
        EquipmentTag::class,
        EquipmentTagDraft::class,
        EquipmentTagDraft.`$`::class,
    ]
)
@Entity
interface EquipmentTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    val name: String

    @ManyToMany(mappedBy = "tags")
    val equipments: List<Equipment>
}