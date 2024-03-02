package io.github.hammerhfut.rehearsal

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.hammerhfut.rehearsal.config.AppConfig
import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.model.dto.LoginData
import io.github.hammerhfut.rehearsal.service.CacheService
import io.github.hammerhfut.rehearsal.util.AuthUtil
import io.github.hammerhfut.rehearsal.util.aesEncrypt
import io.github.hammerhfut.rehearsal.util.generateSecretKeySpec
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import jakarta.ws.rs.core.MediaType
import org.hamcrest.core.Is.`is`
import org.jboss.logging.Logger
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import javax.crypto.spec.SecretKeySpec

/**
 *@author prixii
 *@date 2024/2/14 16:04
 */

@QuarkusTest
@TestMethodOrder(OrderAnnotation::class)
open class AuthResourceTestData {
    @Inject
    private lateinit var appConfig: AppConfig

    @Inject
    private lateinit var objectMapper: ObjectMapper

    @Inject
    private lateinit var cacheService: CacheService

    @Inject
    private lateinit var authUtil: AuthUtil

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
                password = "5569deedc5689d7bd106ed9411e7d5fea81540417a8ec9b23b4d1430d28c832a",
            )
        val loginResponse =
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(loginData))
                .`when`().post("/auth/login")
                .then()
                .statusCode(200)
                .extract().response().jsonPath()
        key = loginResponse.getLong("timestamp") + userTimestamp
        logger.debug("[key]: $key")
        keySpec = generateSecretKeySpec(key)
        utoken = loginResponse.getString("utoken")
        logger.debug("[utoken]: $utoken")
    }

    fun testToken() {
        val msgExpected = "test token"
        val targetPath = "/auth/test/token"
        val urlToken = aesEncrypt(targetPath, keySpec)
        val token = generateToken(urlToken)
        logger.debug("[token]: $token")
        given()
            .header(appConfig.headerAuth(), token)
            .`when`()
            .get(targetPath)
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

        val targetPath = "/auth/test/user"
        val urlToken = aesEncrypt(targetPath, keySpec)
        val token = generateToken(urlToken)
        val user =
            given()
                .header(appConfig.headerAuth(), token)
                .`when`()
                .get(targetPath)
                .then()
                .statusCode(200)
                .extract().response().jsonPath()

        val id = user.getLong("id")
        logger.info("[userId] : $id")
        logger.info("[role] : ${cacheService.getRoleByUserId(id)}")
        logger.info("[user-utoken] : ${cacheService.findUserByUtoken(utoken)}")

        cacheService.invalidateUser(id)
        logger.info("[invalid-user-id] : ${cacheService.findUserById(id)}")
        cacheService.invalidateUser(id)
        logger.info("[invalid-user-utoken] : ${cacheService.findUserByUtoken(utoken)}")
    }

    @Test
    fun testRoleCache() {
        testLogin()

        val targetPath = "/auth/test/role"
        val urlToken = aesEncrypt(targetPath, keySpec)
        val token = generateToken(urlToken)
        val roles =
            given()
                .header(appConfig.headerAuth(), token)
                .`when`()
                .get(targetPath)
                .then()
                .statusCode(200)
                .extract().response().jsonPath()
        logger.info("[role-cache]: $roles")
        val id = cacheService.findUserByUtoken(utoken)?.id ?: throw BusinessError(ErrorCode.NOT_FOUND)
        cacheService.invalidateRole(id)
        logger.info("[invalid-role]: ${cacheService.getRoleByUserId(id)}")
    }
}
