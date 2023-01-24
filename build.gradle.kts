subprojects {
    apply(plugin = "java")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()

        maven { url = uri("https://jitpack.io") }
    }


}
