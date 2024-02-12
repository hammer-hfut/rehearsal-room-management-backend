package io.github.hammerhfut.rehearsal.model.db

import org.babyfish.jimmer.sql.*

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