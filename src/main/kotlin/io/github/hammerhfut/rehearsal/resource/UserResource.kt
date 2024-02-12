package io.github.hammerhfut.rehearsal.resource

import io.github.hammerhfut.rehearsal.model.db.User
import io.github.hammerhfut.rehearsal.model.db.by
import io.smallrye.common.annotation.RunOnVirtualThread
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import java.time.LocalDateTime

@Path("/user")
class UserResource(
    private val sqlClient: KSqlClient
) {
    @Path("/demo")
    @POST
    @RunOnVirtualThread
    fun saveDemoUser() {
        sqlClient.insert(new(User::class).by {
            username = "demo"
            realname = "得摸"
            password = "123"
            createTime = LocalDateTime.now()
            contact = mapOf()
        })
    }
}