// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Versions.kotlin_stdlib_jdk}")
    }
}

//apply(plugin = "com.github.ben-manes.versions")
plugins {
    `update-dependency`
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
