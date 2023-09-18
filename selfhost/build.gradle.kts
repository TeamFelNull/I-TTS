import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("checkstyle")
}

base {
    archivesName.set("itts-selfhost")
}

checkstyle {
    toolVersion = "10.12.2"
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Main-Class" to "dev.felnull.itts.Main")
        attributes("Implementation-Version" to project.version)
    }
}

val shadowIn: Configuration by configurations.creating
configurations {
    shadowIn
    implementation.get().extendsFrom(shadowIn)
}

dependencies {
    shadowIn(project(":core", "default"))

    shadowIn("blue.endless:jankson:1.2.1")
    shadowIn("redis.clients:jedis:4.4.0-m1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.named<ShadowJar>("shadowJar") {
    shadowIn.isTransitive = true
    configurations = listOf(shadowIn)
    archiveClassifier.set("")
    dependencies {
        // include(dependency(":core"))
    }
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}