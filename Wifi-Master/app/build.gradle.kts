import org.gradle.kotlin.dsl.android
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.gms.googleServices)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.devtool.ksp)
    id("kotlin-parcelize")
}

android {
    namespace = "com.wifi.wificharger"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wifi.wificharger"
        minSdk = 24
        targetSdk = 34
        versionCode = 7
        versionName = "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        archivesName = "Wifi-v$versionName($versionCode)"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("keystores/keystores.jks")
            storePassword = "wifi@2024"
            keyAlias = "wifi"
            keyPassword = "wifi@2024"
        }

        getByName("debug") {
            storeFile = rootProject.file("keystores/keystore.jks")
            storePassword = "snaptik"
            keyAlias = "snaptik"
            keyPassword = "snaptik"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    flavorDimensions += "version"

    productFlavors {

        create("prod") {
            applicationId = "com.wifianalyzer.showwifipassword.wifiqr.wifimaster.speedtest"
            manifestPlaceholders["ad_app_id"] = "ca-app-pub-6745384043882937~3781450416"
            buildConfigField("String", "appsflyer_key", "\"m833xY8LzaPiHeuQa6iVqL\"")
            buildConfigField("String", "inter_splash", "\"ca-app-pub-6745384043882937/3186554046\"")
            buildConfigField("String", "inter_home", "\"ca-app-pub-6745384043882937/7745596216\"")
            buildConfigField("String", "app_open", "\"ca-app-pub-6745384043882937/6380543656\"")
            buildConfigField("String", "native_language", "\"ca-app-pub-6745384043882937/3621722210\"")
            buildConfigField("String", "native_language_dup", "\"ca-app-pub-6745384043882937/6399164455\"")
            buildConfigField("String", "native_onboarding", "\"ca-app-pub-6745384043882937/8542822506\"")
            buildConfigField("String", "native_permission", "\"ca-app-pub-6745384043882937/4603577497\"")
            buildConfigField("String", "banner_home", "\"ca-app-pub-6745384043882937/4439979864\"")
            buildConfigField("String", "rewarded_show_password", "\"ca-app-pub-6745384043882937/5725087477\"")
            buildConfigField("String", "native_detail", "\"ca-app-pub-6745384043882937/5485414327\"")
            buildConfigField("String", "native_exit", "\"ca-app-pub-6745384043882937/1180187868\"")
            buildConfigField("String", "native_home", "\"ca-app-pub-6745384043882937/4185155967\"")
        }
        create("dev") {
//            applicationIdSuffix = ".dev"
            manifestPlaceholders["ad_app_id"] = "ca-app-pub-4584260126367940~6547131121"
            buildConfigField("String", "appsflyer_key", "\"m833xY8LzaPiHeuQa6iVqL\"")
            buildConfigField("String", "inter_splash", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "inter_home", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "app_open", "\"ca-app-pub-3940256099942544/9257395921\"")
            buildConfigField("String", "native_language", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_language_dup", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_onboarding", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_permission", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "banner_home", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "rewarded_show_password", "\"ca-app-pub-3940256099942544/5224354917\"")
            buildConfigField("String", "native_detail", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_exit", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_home", "\"ca-app-pub-3940256099942544/2247696110\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    applicationVariants.all {
        outputs.forEach { output ->
            val apkName = "${properties["appName"]}-$versionName-$versionCode-${buildType.name}.apk"
            (output as? com.android.build.gradle.internal.api.BaseVariantOutputImpl)?.apply {
                outputFileName = output.outputFileName.replace(
                    output.outputFileName, apkName
                )
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ads
    implementation(libs.viotech.vioads)
    implementation(libs.androidx.multidex)
    implementation(libs.shimmer)

    //mediation
    implementation(libs.mediation.facebook)
    implementation(libs.mediation.applovin)
    implementation(libs.mediation.pangle)
    implementation(libs.mediation.vungle)
    implementation(libs.mediation.ironsource)
    implementation(libs.mediation.mintegral)
    implementation(libs.mediation.fyber)
    implementation(libs.mediation.inmobi)
    implementation(libs.mediation.unity.ads)
    implementation(libs.mediation.unity)

    // koin
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.koin.android.compat)
    implementation(libs.play.services.ads)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.perf.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.messaging.ktx)

    // Facebook SDK
    implementation(libs.facebook.android.sdk)

    // Appsflyer
    implementation("com.appsflyer:af-android-sdk:6.12.1")
    implementation("com.appsflyer:adrevenue:6.9.0")

    // data store
    implementation(libs.datastore.preferences)

    // coin
    implementation(libs.coil.core)

    // logging
    implementation(libs.timber)

    // lottie
    implementation(libs.lottie)
    implementation(libs.androidx.legacy.support.v4)

    // kotlin serial
    implementation(libs.kotlin.serialization)
    implementation(libs.preference)

    // paging
    implementation(libs.paging)

    // crop
    implementation(libs.android.image.cropper)

    // billing
    implementation(libs.billing.ktx)
    implementation("com.facebook.infer.annotation:infer-annotation:0.18.0")

    // in-app review
    implementation("com.google.android.play:review-ktx:2.0.1")

    // joda time
    implementation(libs.joda.time)

    implementation("io.github.thanosfisherman.wifiutils:wifiutils:1.6.6")
    implementation(libs.androidx.room.ktx)
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("io.github.g00fy2.quickie:quickie-bundled:1.8.0")

    // speedtest
    implementation("fr.bmartel:jspeedtest:1.32.1")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}