package io.github.hammerhfut.rehearsal

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.hammerhfut.rehearsal.config.AppConfig
import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.model.db.dto.CreateRoleData
import io.github.hammerhfut.rehearsal.model.db.dto.CreateRoleGroupDto
import io.github.hammerhfut.rehearsal.model.db.dto.SetUserRolesData
import io.github.hammerhfut.rehearsal.model.dto.*
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
        val newRoleId = createRole(3)
        val (oldSize, _) = testGetAllRoles()
        deleteRole(newRoleId)
        val (newSize, _) = testGetAllRoles()
        logger.info("$oldSize, $newSize")
        if (newSize != null && oldSize != null) {
            assert(oldSize > newSize)
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
        return Pair(rolesJson[2].roles?.size, rolesJson[2].roleGroup?.id)
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
                userId = 1,
                roles =
                    listOf(
                        SetUserRolesData.TargetOf_roles(
                            roleId = 10,
                            bandId = 1,
                        ),
                    ),
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
        val newGroupId = createRoleGroup()
        val roleId = createRole(newGroupId)
        val roleGroupId = getRole(roleId)
        assert(roleGroupId == newGroupId)
    }

    fun createRoleGroup(): Long {
        val path = "/role/group"
        val input = CreateRoleGroupDto(System.currentTimeMillis().toString())
        val result =
            given()
                .header(appConfig.headerAuth(), tokenGenerator(path))
                .body(objectMapper.writeValueAsString(input))
                .contentType(MediaType.APPLICATION_JSON)
                .`when`().post(path)
                .then()
                .extract().response().jsonPath()
        return result.getLong("")
    }

    fun createRole(groupId: Long): Long {
        val path = "/role"
        val input =
            CreateRoleData(
                name = System.currentTimeMillis().toString(),
                remark = "",
                children = listOf(),
                roleGroupId = groupId,
            )
        val result =
            given()
                .header(appConfig.headerAuth(), tokenGenerator(path))
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(input))
                .`when`().post(path)
                .then()
                .extract().response().jsonPath()
        return result.getLong("")
    }

    fun getRole(roleId: Long): Long {
        val path = "/role/$roleId"
        val result =
            given()
                .header(appConfig.headerAuth(), tokenGenerator(path))
                .`when`().get(path)
                .then()
                .extract().response().jsonPath()
        return result.getLong("roleGroup.id")
    }

    fun tokenGenerator(targetPath: String): String {
        val urlToken = aesEncrypt(targetPath, keySpec)
        val token = "Bearer $utoken$urlToken"
        return token
    }
}
