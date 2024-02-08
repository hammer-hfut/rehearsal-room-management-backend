package io.github.hammerhfut.rehearsal.model.db

import com.fasterxml.jackson.annotation.JsonProperty
import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@Entity
@Table(name = "t_user")
interface User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    val username: String
    val realname: String

    @get:JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    val password: String
    val createTime: LocalDateTime
}