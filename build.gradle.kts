plugins {
    id("org.jetbrains.changelog") version "2.0.0"
}

version = if (System.getenv("GITHUB_REF") != null && System.getenv("GITHUB_REF").startsWith("refs/tags/v")) {
    System.getenv("GITHUB_REF").substring("refs/tags/v".length);
} else {
    "NONE"
}

changelog {
    repositoryUrl.set("https://github.com/TeamFelnull/I-TTS")

    introduction.set(
        """
    Changelog to track updates for this mod.  
    Add your changes to Unreleased if you want to commit.  
    Please write according to [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
    """.trimIndent()
    )

    combinePreReleases.set(false)
}

allprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    group = rootProject.group
    version = rootProject.version
}
