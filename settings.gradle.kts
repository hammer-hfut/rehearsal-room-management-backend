pluginManagement {
    val quarkusPluginVersion: String by settings
    val quarkusPluginId: String by settings
    repositories {
        maven {
            url = java.net.URI.create("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        }
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id(quarkusPluginId) version quarkusPluginVersion
    }
}
rootProject.name="rehearsal-room-management-backend"
