import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow") version "8.3.6"
    id("checkstyle")
}

base {
    archivesName.set("itts-selfhost")
}

checkstyle {
    toolVersion = "10.26.1"
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Main-Class" to "dev.felnull.itts.Main")
        attributes("Implementation-Version" to project.version)
    }
}

val shadowIn: Configuration by configurations.creating {
    isTransitive = true
}
configurations {
    implementation.get().extendsFrom(shadowIn)
}

dependencies {
    shadowIn(project(":core", "default"))

    shadowIn("blue.endless:jankson:1.2.3")
    // shadowIn("redis.clients:jedis:4.4.0-m1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(shadowIn)
    archiveClassifier.set("")
    dependencies {
        // include(dependency(":core"))
    }
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}