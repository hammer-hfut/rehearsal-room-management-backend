package io.github.hammerhfut.rehearsal.model.db

import io.quarkus.runtime.annotations.RegisterForReflection
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToManyView
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany

/**
 *@author prixii
 *@date 2024/2/14 19:46
 */

@RegisterForReflection(
    targets = [
        Role::class,
        RoleDraft::class,
        RoleDraft.`$`::class,
    ]
)
@Entity
interface Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long

    @Key
    val name: String

    val remark: String
    val editable: Boolean

    @ManyToOne
    val upperRole: Role?

    @OneToMany(mappedBy = "upperRole")
    val children: List<Role>

    @OneToMany(mappedBy = "role")
    val userRoleBands: List<UserRoleBand>

    @ManyToManyView(
        prop = "userRoleBands",
        deeperProp = "user"
    )
    val users: List<User>

}
