import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

base {
    archivesName.set("itts-selfhost")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Main-Class" to "dev.felnull.itts.Main")
        attributes("Implementation-Version" to project.version)
    }
}

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

val shadowIn by configurations.creating
configurations {
    shadowIn
    implementation.get().extendsFrom(shadowIn)
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

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