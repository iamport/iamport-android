
import Build_gradle.PluginVersion.GRADLE
import Build_gradle.PluginVersion.GRADLE_VERSIONS_PLUGIN
import Build_gradle.PluginVersion.KOTLIN

plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

object PluginVersion {
    const val GRADLE = "7.0.4"
    const val KOTLIN = "1.6.10"
    const val GRADLE_VERSIONS_PLUGIN = "0.41.0"
}

dependencies {
    implementation("com.github.ben-manes:gradle-versions-plugin:${GRADLE_VERSIONS_PLUGIN}")
    implementation("com.android.tools.build:gradle:${GRADLE}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${KOTLIN}")
}