dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation("net.dv8tion:JDA:5.0.0-alpha.22")
    implementation("org.apache.commons:commons-lang3:3.12.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}