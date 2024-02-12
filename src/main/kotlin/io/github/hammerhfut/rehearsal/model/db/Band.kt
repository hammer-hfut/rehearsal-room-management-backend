package io.github.hammerhfut.rehearsal.model.db

import org.babyfish.jimmer.sql.*

@Entity
interface Band {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    val name: String

    @OneToMany(mappedBy = "band")
    val appointments: List<Appointment>

    @ManyToOne
    val leader: User

    @ManyToMany(mappedBy = "joinedBands")
    val members: List<User>
}