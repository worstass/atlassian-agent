import java.net.URI

plugins {
    id("application")
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.forfree.atlassian"
version = "1.0-SNAPSHOT"

repositories {
    maven { url = URI("https://packages.atlassian.com/mvn/maven-atlassian-external") }
    maven { url = URI("https://maven.aliyun.com/repository/central") }
    maven { url = URI("https://maven.aliyun.com/repository/gradle-plugin") }
    maven { url = URI("https://plugins.gradle.org/m2/") }
    mavenCentral()
}

dependencies {
    implementation("com.atlassian.extras:atlassian-extras-key-manager:3.4.6")
    implementation("com.atlassian.extras:atlassian-extras-decoder-api:3.4.6")
    implementation("com.atlassian.extras:atlassian-extras-decoder-v2:3.4.6")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")
    implementation("org.javassist:javassist:3.30.2-GA")
    implementation("com.github.ajalt.clikt:clikt:4.4.0")
    implementation("commons-codec:commons-codec:1.11")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClass,
                "Premain-Class" to "org.forfree.atlassian.Agent"
            )
        )
    }
}

application {
    mainClass = "org.forfree.atlassian.App"
}