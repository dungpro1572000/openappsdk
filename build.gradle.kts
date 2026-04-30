// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.google.services) apply false
}

// === OpenAppSDK Submodule Tasks ===

// Clean: ./gradlew clean already cleans all included modules (including :openappsdk)

// Build release AAR
tasks.register("buildOpenAppSdk") {
    group = "openappsdk"
    description = "Build the release variant of openappsdk submodule"
    dependsOn(":openappsdk:assembleRelease")
}

// Publish to Maven Local (for testing before pushing to JitPack)
tasks.register("publishOpenAppSdkLocal") {
    group = "openappsdk"
    description = "Publish openappsdk release to Maven Local"
    dependsOn(":openappsdk:publishReleasePublicationToMavenLocal")
}

// Publish to JitPack: update version, commit, tag, and push
// Usage: ./gradlew publishOpenAppSdkToJitPack -PsdkVersion=1.1.0
tasks.register("publishOpenAppSdkToJitPack") {
    group = "openappsdk"
    description = "Update version in build.gradle.kts, commit, tag, and push to trigger JitPack build"
    doLast {
        val version = project.findProperty("sdkVersion")?.toString()
            ?: throw GradleException("Please specify version: ./gradlew publishOpenAppSdkToJitPack -PsdkVersion=1.1.0")

        // 1. Update version in openappsdk/build.gradle.kts
        val buildFile = file("openappsdk/build.gradle.kts")
        val content = buildFile.readText()
        val updated = content.replace(
            Regex("""version\s*=\s*"[^"]+""""),
            """version = "$version""""
        )
        if (content == updated) {
            throw GradleException("Could not find version string in openappsdk/build.gradle.kts")
        }
        buildFile.writeText(updated)
        logger.lifecycle("Updated openappsdk version to $version")

        // 2. Git add + commit
        exec { commandLine("git", "add", "openappsdk/build.gradle.kts") }
        exec { commandLine("git", "commit", "-m", "Bump openappsdk version to $version") }
        logger.lifecycle("Committed version bump")

        // 3. Create tag
        exec { commandLine("git", "tag", version) }
        logger.lifecycle("Created tag $version")

        // 4. Push commit + tag
        exec { commandLine("git", "push", "origin", "main", "--tags") }
        logger.lifecycle("Pushed to origin. JitPack will build at: https://jitpack.io/#dungpro1572000/openappsdk/$version")
    }
}
