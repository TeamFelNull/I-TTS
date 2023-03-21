plugins {
    `maven-publish`
}

base {
    archivesName.set("itts-core")
}

dependencies {
    //https://github.com/DV8FromTheWorld/JDA/pull/2240 <-音声の遅れが生じる可能あり
    implementation("net.dv8tion:JDA:5.0.0-beta.6")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.apache.logging.log4j:log4j-core:2.18.0")
    implementation("dev.felnull:felnull-java-library:1.75")
    implementation("com.github.walkyst:lavaplayer-fork:1.3.99.2")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.ibm.icu:icu4j:72.1")

    implementation("org.jetbrains:annotations:23.0.0")
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
                password = project.extra["maven_put_pass"]?.toString() ?: System.getenv("mavenpassword")
            }
        }
    }
}