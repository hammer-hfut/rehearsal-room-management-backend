package io.github.hammerhfut.rehearsal.model.db

import org.babyfish.jimmer.sql.*

@Entity
interface Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    val name: String
}