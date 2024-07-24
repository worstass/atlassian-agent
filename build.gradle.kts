import java.net.URI

plugins {
    id("application")
    kotlin("jvm") version "2.0.0"
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
    implementation("org.slf4j:slf4j-simple:2.0.13")
    implementation("org.javassist:javassist:3.30.2-GA")
    implementation("com.github.ajalt.clikt:clikt:4.4.0")
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
                "Main-Class" to "org.forfree.atlassianv.App",
                "Premain-Class" to "org.forfree.atlassian.Agent"
            )
        )
    }
}

application {
    mainClass = "org.forfree.atlassian.App"
}