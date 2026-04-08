// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
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

// Publish to JitPack: push a tag to the openappsdk submodule repo
// Example: cd openappsdk && git tag 1.0.1 && git push origin 1.0.1
tasks.register<Exec>("publishOpenAppSdkToJitPack") {
    group = "openappsdk"
    description = "Tag and push the openappsdk submodule to trigger JitPack build"
    workingDir = file("openappsdk")
    doFirst {
        val tag = project.findProperty("sdkVersion")?.toString()
            ?: throw GradleException("Please specify version: ./gradlew publishOpenAppSdkToJitPack -PsdkVersion=1.0.1")
        commandLine("git", "tag", tag)
    }
    commandLine("git", "push", "origin", "--tags")
}
