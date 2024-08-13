plugins {
    id("maven-publish")
    id("checkstyle")
}

base {
    archivesName.set("itts-core")
}

checkstyle {
    toolVersion = "10.12.2"
}

dependencies {
    //https://github.com/DV8FromTheWorld/JDA/pull/2240 <-音声の遅れが生じる可能あり
    api("net.dv8tion:JDA:5.0.2")
    api("org.apache.commons:commons-lang3:3.16.0")
    api("com.google.code.gson:gson:2.11.0")
    api("com.google.guava:guava:33.2.1-jre")
    api("org.apache.logging.log4j:log4j-core:3.0.0-beta2")
    api("dev.felnull:felnull-java-library:1.75")
    api("dev.arbjerg:lavaplayer:2.2.1")
    api("commons-io:commons-io:2.16.1")
    api("com.ibm.icu:icu4j:75.1")
    api("com.atilika.kuromoji:kuromoji-ipadic:0.9.0")
    api("it.unimi.dsi:fastutil:8.5.14")

    api("org.jetbrains:annotations:23.0.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
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