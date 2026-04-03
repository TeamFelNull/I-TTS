plugins {
    id("java")
    id("org.jetbrains.changelog") version "2.0.0"
}

version = if (System.getenv("GITHUB_REF") != null && System.getenv("GITHUB_REF").startsWith("refs/tags/v")) {
    System.getenv("GITHUB_REF").substring("refs/tags/v".length)
} else {
    "NONE"
}

changelog {
    repositoryUrl.set("https://github.com/TeamFelnull/I-TTS")

    introduction.set(
        """
    このBOTの更新を追跡するための更新ログ。   
    変更をコミットする場合は`Unreleased`に更新内容を追記してください。  
    [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)に従って記述をお願い致します。  
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

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}