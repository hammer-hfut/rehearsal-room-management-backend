package io.github.hammerhfut.rehearsal.resource

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.hammerhfut.rehearsal.model.db.User
import io.github.hammerhfut.rehearsal.model.db.fetchBy
import io.github.hammerhfut.rehearsal.model.db.username
import io.github.hammerhfut.rehearsal.service.AuthService
import io.smallrye.common.annotation.RunOnVirtualThread
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.mindrot.jbcrypt.BCrypt

/**
 *@author prixii
 *@date 2024/2/12 13:10
 */

const val LIFETIME: Long = 114514

@Path("/auth")
class AuthResource(
    private val sqlClient: KSqlClient,
    private val authService: AuthService
) {
    @POST
    @RunOnVirtualThread
    fun login(input: LoginData): LoginResponse {
        val user = sqlClient.createQuery(User::class) {
            where(table.username.eq(input.uid))
            select(
                table.fetchBy {
                    allScalarFields()
                }
            )
        }.fetchOneOrNull()
            ?.takeIf {BCrypt.checkpw(input.password, it.password)}
            ?: throw Error() // TODO 异常处理
        val uToken= authService.generateUToken(user.username.toLong(), input.timestamp)
        return LoginResponse(uToken, LIFETIME)
    }



}


data class LoginData(
    val uid: String,
    val password: String,
    val timestamp: Long
)

data class LoginResponse(
    val uToken: String,
    val lifetime: Long
)

data class UTokenCacheData(
    val id: Long,
    val lifetime: Long,
    val key: Long
)
