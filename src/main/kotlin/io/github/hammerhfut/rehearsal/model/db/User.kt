@file:Suppress("ktlint:standard:no-wildcard-imports")

package io.github.hammerhfut.rehearsal.model.db

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection
import org.babyfish.jimmer.sql.*
import java.time.LocalDateTime

@RegisterForReflection(
    targets = [
        User::class,
        UserDraft::class,
        UserDraft.`$`::class,
    ],
)
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
    val joinTime: LocalDateTime?
    val expireTime: LocalDateTime?
    val expireComment: String

    @Serialized
    val contact: Map<String, String>

    @ManyToMany
    @JoinTable(name = "band_user_mapping", joinColumnName = "user_id")
    val joinedBands: List<Band>

    @OneToMany(mappedBy = "owner")
    val equipments: List<Equipment>

    @OneToMany(mappedBy = "receiver")
    val notices: List<LogNotice>

    @OneToMany(mappedBy = "user")
    val userRoleBands: List<UserRoleBand>

    @ManyToManyView(
        prop = "userRoleBands",
        deeperProp = "role",
    )
    val roles: List<Role>
}
