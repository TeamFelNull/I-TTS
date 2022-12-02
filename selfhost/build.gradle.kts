dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation(project(":core"))
    implementation("blue.endless:jankson:1.2.1")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.apache.logging.log4j:log4j-core:2.18.0")
    implementation("dev.felnull:felnull-java-library:1.73")

    implementation("org.jetbrains:annotations:23.0.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}