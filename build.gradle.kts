import java.net.URI

plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.allopen") version "1.9.21"
    id("io.quarkus")
    id("com.google.devtools.ksp") version "1.9.21-1.0.16"
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

repositories {
    maven {
        url = URI.create("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
    }
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    val jimmerVersion = "0.8.87"
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    // 序列化
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    // 验证
    implementation("io.quarkus:quarkus-hibernate-validator")
    // reactive
    implementation("io.quarkus:quarkus-mutiny")
    implementation("io.smallrye.reactive:mutiny-kotlin:2.5.3")
    // kotlin
    implementation("io.quarkus:quarkus-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
    // yaml 配置
    implementation("io.quarkus:quarkus-config-yaml")
    // 数据库
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("org.babyfish.jimmer:jimmer-sql-kotlin:$jimmerVersion")
    ksp("org.babyfish.jimmer:jimmer-ksp:$jimmerVersion")
    implementation("io.quarkus:quarkus-agroal")
    // 缓存
    implementation("io.quarkus:quarkus-cache")
    // di
    implementation("io.quarkus:quarkus-arc")
    // 安全
    implementation("io.quarkus:quarkus-security")
    // web
    implementation("io.quarkus:quarkus-resteasy-reactive")
    // 测试
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "io.github.hammerhfut"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_21.toString()
    kotlinOptions.javaParameters = true
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("detekt-config.yml"))
    ignoreFailures = true
}

project.afterEvaluate {
    getTasksByName("quarkusGenerateCode", true).forEach { task ->
        task.setDependsOn(
            task.dependsOn.filterIsInstance<Provider<Task>>().filter { it.get().name != "processResources" })
    }
    getTasksByName("quarkusGenerateCodeDev", true).forEach { task ->
        task.setDependsOn(
            task.dependsOn.filterIsInstance<Provider<Task>>().filter { it.get().name != "processResources" })
    }
}