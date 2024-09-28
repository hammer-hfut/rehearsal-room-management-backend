package io.github.hammerhfut.rehearsal

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.hammerhfut.rehearsal.config.AppConfig
import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.model.dto.LoginData
import io.github.hammerhfut.rehearsal.service.UserInfoCacheService
import io.github.hammerhfut.rehearsal.util.aesEncrypt
import io.github.hammerhfut.rehearsal.util.generateSecretKeySpec
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import jakarta.ws.rs.core.MediaType
import org.hamcrest.core.Is.`is`
import org.jboss.logging.Logger
import org.junit.jupiter.api.Test
import javax.crypto.spec.SecretKeySpec

/**
 *@author prixii
 *@date 2024/2/14 16:04
 */

@QuarkusTest
open class AuthResourceTest {
    @Inject
    private lateinit var appConfig: AppConfig

    @Inject
    private lateinit var objectMapper: ObjectMapper

    @Inject
    private lateinit var userInfoCacheService: UserInfoCacheService

    var key: Long = 0
    var utoken: String = ""
    lateinit var keySpec: SecretKeySpec
    val logger: Logger = Logger.getLogger("[auth-test]")

    @Test
    fun testAuth() {
        testLogin()
        testToken()
        testRefreshToken()
    }

    fun testLogin() {
        val userTimestamp = System.currentTimeMillis()
        val loginData =
            LoginData(
                username = "s114514",
                timestamp = userTimestamp,
                password = "e10adc3949ba59abbe56e057f20f883e",
            )
        val loginResponse =
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(loginData))
                .`when`()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .response()
                .jsonPath()
        key = loginResponse.getLong("timestamp") + userTimestamp
        logger.debug("[key]: $key")
        keySpec = generateSecretKeySpec(key)
        utoken = loginResponse.getString("utoken")
        logger.debug("[utoken]: $utoken")
    }

    fun testToken() {
        val msgExpected = "test token"
        val path = "/auth/test/token"
        val urlToken = aesEncrypt(path, keySpec)
        val token = generateToken(urlToken)
        logger.debug("[token]: $token")
        given()
            .header(appConfig.headerAuth(), token)
            .`when`()
            .get(path)
            .then()
            .statusCode(200)
            .body(`is`(msgExpected))
    }

    fun testRefreshToken() {
        val targetPath = "/auth/refresh/$key"
        val urlToken = aesEncrypt(targetPath, keySpec)
        val token = generateToken(urlToken)
        given()
            .header(appConfig.headerAuth(), token)
            .`when`()
            .put(targetPath)
            .then()
            .statusCode(200)
    }

    fun generateToken(urlToken: String) = "Bearer $utoken$urlToken"

    @Test
    fun testUserCache() {
        testLogin()

        val path = "/auth/test/user"
        val urlToken = aesEncrypt(path, keySpec)
        val token = generateToken(urlToken)
        val user =
            given()
                .header(appConfig.headerAuth(), token)
                .`when`()
                .get(path)
                .then()
                .statusCode(200)
                .extract()
                .response()
                .jsonPath()

        val id = user.getLong("id")
        logger.info("[userId] : $id")
        logger.info("[role] : ${userInfoCacheService.getRoleByUserId(id)}")
        logger.info("[user-utoken] : ${userInfoCacheService.findUserByUtoken(utoken)}")
    }

    @Test
    fun testRoleCache() {
        testLogin()

        val path = "/auth/test/role"
        val urlToken = aesEncrypt(path, keySpec)
        val token = generateToken(urlToken)
        val roles =
            given()
                .header(appConfig.headerAuth(), token)
                .`when`()
                .get(path)
                .then()
                .statusCode(200)
                .extract()
                .response()
                .jsonPath()
        logger.info("[role-cache]: $roles")
        userInfoCacheService.findUserByUtoken(utoken)?.id ?: throw BusinessError(ErrorCode.NOT_FOUND)
    }
}
