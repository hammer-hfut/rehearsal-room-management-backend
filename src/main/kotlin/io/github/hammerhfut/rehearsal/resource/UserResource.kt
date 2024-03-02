package io.github.hammerhfut.rehearsal.resource

import io.github.hammerhfut.rehearsal.model.db.User
import io.github.hammerhfut.rehearsal.model.db.by
import io.smallrye.common.annotation.RunOnVirtualThread
import jakarta.ws.rs.*
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import java.time.LocalDateTime
import java.security.SecureRandom
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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

    @POST
    @RunOnVirtualThread
    fun createUser(
        newUser: CreateUserRequest
    ): Response {
        return try {
            // 生成强密码
            val password = generateStrongPassword()

            // 创建新用户实例
            val user = new(User::class).by {
                username = newUser.username
                realname = newUser.realname
                this.password = password
                createTime = LocalDateTime.now()
                contact = newUser.contact
            }

            // 将新用户保存到数据库中
            val savedUser = sqlClient.insert(user)

            // 确认用户保存成功后再返回用户信息和密码
            if (savedUser.isModified != null) {
                val userResponse = UserResponse(
                    id = user.id,
                    username = user.username,
                    realname = user.realname,
                    contact = user.contact,
                    password = password
                )
                Response.ok(userResponse).build()
            } else {
                throw Exception("Failed to save user")
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    // 生成强密码
    private fun generateStrongPassword(): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()"
        val random = SecureRandom()
        return (1..12)
            .map { charset[random.nextInt(charset.length)] }
            .joinToString("")
    }

    // 处理异常
    private fun handleException(e: Exception): Response {
        val errorMessage = "{\"error\": \"${e.message ?: "Internal server error"}\"}"
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build()
    }

    //请求信息类
    data class CreateUserRequest(
        val username: String,
        val realname: String,
        val contact: Map<String, String>
    )

    //返回响应类
    data class UserResponse(
        val id: Long,
        val username: String,
        val realname: String,
        val contact: Map<String, String>,
        val password: String
    )
}