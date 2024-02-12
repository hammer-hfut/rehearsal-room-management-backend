package io.github.hammerhfut.rehearsal.model.db

import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
interface Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long
    val name: String
    val startTime: LocalDateTime
    val endTime: LocalDateTime
    @ManyToOne
    val place: Place
    @ManyToOne
    val band: Band
}