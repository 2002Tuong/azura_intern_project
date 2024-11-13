// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.gmsGoogleServices) apply false
    alias(libs.plugins.firebaseCrashlytics) apply false
    alias(libs.plugins.firebasePerf) apply false
    alias(libs.plugins.devtoolKsp) apply false
    alias(libs.plugins.gradle.versions)
    alias(libs.plugins.versionCatalogUpdate)

}

apply("${project.rootDir}/buildscripts/toml-updater-config.gradle")
