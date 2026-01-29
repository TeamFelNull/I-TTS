plugins {
    id("maven-publish")
    id("checkstyle")
}

base {
    archivesName.set("itts-core")
}

checkstyle {
    toolVersion = "10.26.1"
    sourceSets = listOf(project.sourceSets.getByName("main"))
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-junit-jupiter:5.21.0")

    api("net.dv8tion:JDA:6.3.0")
    api("org.apache.commons:commons-lang3:3.20.0")
    api("com.google.code.gson:gson:2.13.2")
    api("com.google.guava:guava:33.5.0-jre")
    api("dev.felnull:felnull-java-library:1.75")
    api("dev.arbjerg:lavaplayer:2.2.6")
    api("club.minnced:jdave-api:0.1.5")
    runtimeOnly("club.minnced:jdave-native-linux-x86-64:0.1.5")
    runtimeOnly("club.minnced:jdave-native-linux-aarch64:0.1.5")
    runtimeOnly("club.minnced:jdave-native-win-x86-64:0.1.5")
    runtimeOnly("club.minnced:jdave-native-darwin:0.1.5")
    api("commons-io:commons-io:2.20.0")
    api("com.ibm.icu:icu4j:77.1")
    api("com.atilika.kuromoji:kuromoji-ipadic:0.9.0")
    api("com.zaxxer:HikariCP:7.0.2")
    api("com.mysql:mysql-connector-j:9.3.0")
    api("org.xerial:sqlite-jdbc:3.51.1.0")
    api("it.unimi.dsi:fastutil:8.5.18")

    api("org.jetbrains:annotations:26.0.2-1")

    // api("org.apache.logging.log4j:log4j-core:3.0.0-beta3")
    api("org.apache.logging.log4j:log4j-core:2.25.3")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = base.archivesName.get()
            from(components["java"])

            pom {
                name.set("ITTSCore")
                description.set("The ikisugi discord tts bot")
                developers {
                    developer {
                        id.set("MORIMORI0317")
                        name.set("MORIMORI0317")
                    }
                    developer {
                        id.set("FelNull")
                        name.set("TeamFelNull")
                        email.set("teamfelnull@felnull.dev")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(project.extra["maven_put_url"].toString())
            credentials {
                username = "felnull"
                password = System.getenv("mavenpassword")
            }
        }
    }
}