package io.github.hammerhfut.rehearsal

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.hammerhfut.rehearsal.config.AppConfig
import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.model.dto.GetAllRolesResponseElement
import io.github.hammerhfut.rehearsal.model.dto.LoginData
import io.github.hammerhfut.rehearsal.model.dto.RoleBand
import io.github.hammerhfut.rehearsal.model.dto.SetUserRolesData
import io.github.hammerhfut.rehearsal.service.CacheService
import io.github.hammerhfut.rehearsal.util.aesEncrypt
import io.github.hammerhfut.rehearsal.util.generateSecretKeySpec
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import jakarta.ws.rs.core.MediaType
import org.jboss.logging.Logger
import org.junit.jupiter.api.Test
import javax.crypto.spec.SecretKeySpec

/**
 *@author prixii
 *@date 2024/3/3 9:51
 */

@QuarkusTest
open class RoleResourceTest {
    @Inject
    private lateinit var appConfig: AppConfig

    @Inject
    private lateinit var objectMapper: ObjectMapper

    @Inject
    private lateinit var cacheService: CacheService

    var key: Long = 0
    var utoken: String = ""
    lateinit var keySpec: SecretKeySpec
    val logger: Logger = Logger.getLogger("[role-test]")

    @Test
    fun testGetRole() {
        testLogin()
        val (oldSize, lastRoleId) = testGetAllRoles()
        logger.info("$oldSize, $lastRoleId")
        lastRoleId?.let { deleteRole(it) }
        val (newSize, _) = testGetAllRoles()
        logger.info("$newSize")
        if (newSize != null) {
            assert(oldSize == newSize.plus(1))
        } else {
            throw BusinessError(ErrorCode.NOT_FOUND)
        }
    }

    @Test
    fun testSetUserRole() {
        testLogin()
        getRolesByUserId(1)
        getCache()
        setUserRoles()
        getRolesByUserId(1)
        getCache()
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

    fun testGetAllRoles(): Pair<Int?, Long?> {
        val path = "/role"
        val result =
            given()
                .header(appConfig.headerAuth(), tokenGenerator(path))
                .`when`().get(path)
                .then()
                .statusCode(200)
                .extract().response().jsonPath()

        val jsonString = objectMapper.writeValueAsString(result.getList<GetAllRolesResponseElement>(""))
        val rolesJson = objectMapper.readValue(jsonString, object : TypeReference<List<GetAllRolesResponseElement>>() {})
        return Pair(rolesJson.last()?.roles?.size, rolesJson.last().roles?.last()?.id)
    }

    fun getRolesByUserId(userId: Long) {
        val path = "/role/user/$userId"
        given()
            .header(appConfig.headerAuth(), tokenGenerator(path))
            .`when`().get(path)
            .then()
            .statusCode(200)
            .extract().response().jsonPath()
    }

    fun setUserRoles() {
        val path = "/role/user"
        val input =
            SetUserRolesData(
                roles = listOf(RoleBand(roleId = 10, bandId = 1)),
                userId = 1,
            )
        given()
            .header(appConfig.headerAuth(), tokenGenerator(path))
            .contentType(MediaType.APPLICATION_JSON)
            .body(objectMapper.writeValueAsString(input))
            .`when`().put(path)
            .then()
            .statusCode(204)
        logger.info("[setUserRoles]: set")
    }

    fun getCache() {
        logger.info("[CacheSize]: ${cacheService.getRoleByUserId(1)?.size}")
    }

    fun deleteRole(id: Long) {
        val path = "/role/$id"
        given()
            .header(appConfig.headerAuth(), tokenGenerator(path))
            .`when`().delete(path)
            .then()
            .statusCode(204)
    }

    @Test
    fun testRoleGroup() {
        testLogin()
        createRoleGroup()
//        添加role到分组
//        移动role到分组
    }

    fun createRoleGroup() {
        val path = ""
    }

    fun tokenGenerator(targetPath: String): String {
        val urlToken = aesEncrypt(targetPath, keySpec)
        val token = "Bearer $utoken$urlToken"
        return token
    }
}
