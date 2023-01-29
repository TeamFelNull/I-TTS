
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation("net.dv8tion:JDA:5.0.0-beta.3")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.apache.logging.log4j:log4j-core:2.18.0")
    implementation("dev.felnull:felnull-java-library:1.75")
    implementation("com.github.walkyst:lavaplayer-fork:1.3.99.2")
    implementation("commons-io:commons-io:2.11.0")

    implementation("org.jetbrains:annotations:23.0.0")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}