plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

object PluginVersion {
    const val GRADLE = "8.1.2"
    const val KOTLIN = "1.9.10"
    const val GRADLE_VERSIONS_PLUGIN = "0.49.0"
}

dependencies {
    implementation("com.github.ben-manes:gradle-versions-plugin:${PluginVersion.GRADLE_VERSIONS_PLUGIN}")
    implementation("com.android.tools.build:gradle:${PluginVersion.GRADLE}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${PluginVersion.KOTLIN}")
}