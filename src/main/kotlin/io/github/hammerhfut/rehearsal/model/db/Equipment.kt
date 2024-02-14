package io.github.hammerhfut.rehearsal.model.db

import io.quarkus.runtime.annotations.RegisterForReflection
import org.babyfish.jimmer.sql.*

@RegisterForReflection(
    targets = [
        Equipment::class,
        EquipmentDraft::class,
        EquipmentDraft.`$`::class,
    ]
)
@Entity
interface Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long
    val name: String
    val requirementText: String
    val comment: String

    @ManyToOne
    val owner: User

    @ManyToMany
    @JoinTable(name = "equipment_tag_mapping", inverseJoinColumnName = "tag_id")
    val tags: List<EquipmentTag>
}