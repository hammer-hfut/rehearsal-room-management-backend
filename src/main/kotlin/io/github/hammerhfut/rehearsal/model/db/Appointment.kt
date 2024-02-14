package io.github.hammerhfut.rehearsal.model.db

import io.quarkus.runtime.annotations.RegisterForReflection
import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@RegisterForReflection(
    targets = [
        Appointment::class,
        AppointmentDraft::class,
        AppointmentDraft.`$`::class,
    ]
)
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