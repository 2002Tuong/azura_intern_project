@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.videoart.batterychargeranimation"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.videoart.batterychargeranimation"
        minSdk = 24
        targetSdk = 34
        versionCode = 11
        versionName = "1.0.9"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("keystores/keystores.jks")
            storePassword = "Battery@2024"
            keyAlias = "battery"
            keyPassword = "Battery@2024"
        }
        getByName("debug") {
            storeFile = rootProject.file("keystores/keystores.jks")
            storePassword = "Battery@2024"
            keyAlias = "battery"
            keyPassword = "Battery@2024"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        buildConfig = true
    }

    flavorDimensions += "env"
    productFlavors {
        create("STAG") {
            manifestPlaceholders["ad_app_id"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("boolean", "is_debug", "true")
            buildConfigField("String", "inter_splash", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "inter_select", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "inter_battery_info", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "native_language","\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_language_dup","\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_onboarding","\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_permission","\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_home","\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_preview","\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_battery_info","\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_exit","\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "native_charging","\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "banner_main","\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField ("String", "appopen_resume", "\"ca-app-pub-3940256099942544/9257395921\"")

            buildConfigField("String", "appsflyer_key", "\"m833xY8LzaPiHeuQa6iVqL\"")
        }

        create("PROD") {
            applicationId = "com.batterycharge.charginganimation.batterychargingeffect"
            buildConfigField("boolean", "is_debug", "false")
            manifestPlaceholders["ad_app_id"] = "ca-app-pub-6745384043882937~3730205870"
            buildConfigField("String", "inter_splash", "\"ca-app-pub-6745384043882937/1333737702\"")
            buildConfigField("String", "inter_select", "\"ca-app-pub-6745384043882937/5925124371\"")
            buildConfigField("String", "inter_battery_info", "\"ca-app-pub-6745384043882937/2945352214\"")
            buildConfigField("String", "native_language","\"ca-app-pub-6745384043882937/6421562790\"")
            buildConfigField("String", "native_language_dup","\"ca-app-pub-6745384043882937/8266605857\"")
            buildConfigField("String", "native_onboarding","\"ca-app-pub-6745384043882937/5108481124\"")
            buildConfigField("String", "native_permission","\"ca-app-pub-6745384043882937/7238206044\"")
            buildConfigField("String", "native_home","\"ca-app-pub-6745384043882937/2919687087\"")
            buildConfigField("String", "native_preview","\"ca-app-pub-6745384043882937/8856154442\"")
            buildConfigField("String", "native_battery_info","\"ca-app-pub-6745384043882937/3298961030\"")
            buildConfigField("String", "native_exit","\"ca-app-pub-6745384043882937/2073303411\"")
            buildConfigField ("String", "native_charging", "\"ca-app-pub-6745384043882937/8851438528\"")
            buildConfigField("String", "banner_main","\"ca-app-pub-6745384043882937/1169236112\"")
            buildConfigField ("String", "appopen_resume", "\"ca-app-pub-6745384043882937/4232768754\"")

            buildConfigField("String", "appsflyer_key", "\"m833xY8LzaPiHeuQa6iVqL\"")
        }
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)


    implementation (platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-firestore-ktx")

    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.process)

    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    implementation(libs.legacy.support.v4)

    implementation(libs.firebase.config.ktx)

    implementation(libs.circleimageview)
    implementation(libs.glide)


    implementation(libs.gson)
    implementation(libs.preference.ktx)
    implementation(libs.play.services.ads)

    implementation(libs.koin.android)
    implementation(libs.koin.android.compat)
    implementation(libs.koin.core)

    implementation(libs.viewbindingpropertydelegate)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    implementation(libs.bottom.navigation.indicator)
    implementation(libs.lottie)

    implementation ("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation ("com.google.android.exoplayer:exoplayer-core:2.19.1")

    //admob
    implementation ("com.github.VioTechDev:VioAds:0.0.5")
    implementation ("androidx.multidex:multidex:2.0.1")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")

    // mediation
    implementation("com.google.ads.mediation:facebook:6.16.0.0")
    implementation("com.google.ads.mediation:applovin:11.11.3.0")
    implementation("com.google.ads.mediation:pangle:5.5.0.9.0")
    implementation("com.google.ads.mediation:vungle:7.0.0.1")
    implementation("com.google.ads.mediation:ironsource:7.5.2.0")
    implementation("com.google.ads.mediation:mintegral:16.5.51.0")
    implementation("com.google.ads.mediation:fyber:8.2.4.0")
    implementation("com.google.ads.mediation:inmobi:10.6.2.0")
    implementation("com.unity3d.ads:unity-ads:4.8.0")
    implementation("com.google.ads.mediation:unity:4.9.2.0")
    implementation("com.android.billingclient:billing:6.0.1")


    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")

    implementation ("com.github.haseebazeem15:gifImageView:1.4")

    implementation("io.coil-kt:coil:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")

    implementation ("com.google.android.play:review-ktx:2.0.1")
    implementation("com.google.android.ump:user-messaging-platform:2.1.0")

    // Facebook SDK
    implementation("com.facebook.android:facebook-android-sdk:16.3.0")

    // Appsflyer
    implementation("com.appsflyer:af-android-sdk:6.12.1")
    implementation("com.appsflyer:adrevenue:6.9.0")
}