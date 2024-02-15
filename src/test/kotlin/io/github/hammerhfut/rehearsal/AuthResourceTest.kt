package io.github.hammerhfut.rehearsal

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.hammerhfut.rehearsal.model.dto.LoginData
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.ws.rs.core.MediaType
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test

/**
 *@author prixii
 *@date 2024/2/14 16:04
 */

@QuarkusTest
open class AuthResourceTest {
    val objectMapper = ObjectMapper()

    @Test
    fun testLogin() {
        val objectMapper = ObjectMapper()
        val loginData = LoginData(
            username = "s114514",
            timestamp = System.currentTimeMillis(),
            password = "5569deedc5689d7bd106ed9411e7d5fea81540417a8ec9b23b4d1430d28c832a"
        )
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(objectMapper.writeValueAsString(loginData))
            .`when`().post("/auth/login")
            .then()
            .statusCode(200)
    }
}
