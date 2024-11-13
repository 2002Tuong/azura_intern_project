@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.firebaseCrashlytics)
    alias(libs.plugins.gmsGoogleServices)
//    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.devtoolKsp)
    id("kotlin-parcelize")
}

android {
    namespace = "com.screentheme.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.screentheme.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    flavorDimensions.add("version")
    productFlavors {
        create("prod") {
            manifestPlaceholders["ad_app_id"]="ca-app-pub-6745384043882937~8328252439"
            buildConfigField ("String", "inter_splash", "\"ca-app-pub-6745384043882937/6042668748\"")
            buildConfigField ("String", "native_splash", "\"ca-app-pub-6745384043882937/1750963691\"")
            buildConfigField ("String", "native_home", "\"ca-app-pub-6745384043882937/2105099172\"")
            buildConfigField ("String", "banner_home", "\"ca-app-pub-6745384043882937/1532562187\"")
            buildConfigField ("String", "banner_theme", "\"ca-app-pub-6745384043882937/5155411726\"")
            buildConfigField ("String", "banner_customize", "\"ca-app-pub-6745384043882937/5648551530\"")
            buildConfigField ("String", "appopen_resume", "\"ca-app-pub-6745384043882937/9307110420\"")
            buildConfigField ("String", "native_language", "\"ca-app-pub-6745384043882937/3388569339\"")
            buildConfigField ("String", "native_language_dup", "\"ca-app-pub-6745384043882937/2411443286\"")
            buildConfigField ("String", "native_onboarding", "\"ca-app-pub-6745384043882937/2075487664\"")
            buildConfigField ("String", "native_permission", "\"ca-app-pub-6745384043882937/5851097057\"")
            buildConfigField ("String", "inter_theme", "\"ca-app-pub-6745384043882937/8449324325\"")
            buildConfigField ("String", "inter_customize", "\"ca-app-pub-6745384043882937/4402268108\"")
            buildConfigField ("String", "native_save_theme", "\"ca-app-pub-6745384043882937/8257752631\"")
            buildConfigField ("String", "native_set_call_theme", "\"ca-app-pub-6745384043882937/5855588605\"")
            buildConfigField ("String", "collapsible_banner_home", "\"ca-app-pub-5502309187737198/9416595131\"")
            buildConfigField ("String", "inter_splash_high", "\"ca-app-pub-5502309187737198/5184079345\"")
            buildConfigField ("String", "inter_splash_medium", "\"ca-app-pub-5502309187737198/9590228215\"")
            buildConfigField ("String", "native_language_high", "\"ca-app-pub-5502309187737198/1844396295\"")
            buildConfigField ("String", "native_language_medium", "\"ca-app-pub-5502309187737198/4146329848\"")
            buildConfigField ("String", "native_onboarding_high", "\"ca-app-pub-5502309187737198/9861690953\"")
            buildConfigField ("String", "native_onboarding_medium", "\"ca-app-pub-5502309187737198/9750806472\"")
            buildConfigField ("String", "inter_choose_avatar", "\"ca-app-pub-6745384043882937/6836875663\"")
            buildConfigField ("String", "inter_choose_icon", "\"ca-app-pub-6745384043882937/6094639633\"")
            buildConfigField ("String", "inter_choose_background", "\"ca-app-pub-6745384043882937/8529231281\"")
            buildConfigField ("String", "inter_preview", "\"ca-app-pub-6745384043882937/6645303972\"")
            buildConfigField ("String", "inter_preview_video", "\"ca-app-pub-6745384043882937/4295370594\"")
            buildConfigField ("String", "inter_category", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField ("String", "rewarded_set_call_theme", "\"ca-app-pub-6745384043882937/2059813826\"")
        }

        create("dev") {
            applicationId = "com.screentheme.app"
            manifestPlaceholders["ad_app_id"]="ca-app-pub-4584260126367940~6547131121"
            buildConfigField ("String", "inter_splash", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField ("String", "native_splash", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField ("String", "native_home", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField ("String", "inter_splash_high", "\"ca-app-pub-3940256099942544/8691691433\"")
            buildConfigField ("String", "inter_splash_medium", "\"ca-app-pub-3940256099942544/8691691433\"")
            buildConfigField ("String", "banner_home", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField ("String", "banner_theme", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField ("String", "banner_customize", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField ("String", "appopen_resume", "\"ca-app-pub-3940256099942544/9257395921\"")
            buildConfigField ("String", "native_permission", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField ("String", "native_language", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField ("String", "native_language_dup", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField ("String", "native_language_high", "\"ca-app-pub-3940256099942544/1044960115\"")
            buildConfigField ("String", "native_language_medium", "\"ca-app-pub-3940256099942544/1044960115\"")
            buildConfigField ("String", "native_onboarding", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField ("String", "native_onboarding_high", "\"ca-app-pub-3940256099942544/1044960115\"")
            buildConfigField ("String", "native_onboarding_medium", "\"ca-app-pub-3940256099942544/1044960115\"")
            buildConfigField ("String", "inter_theme", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField ("String", "inter_customize", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField ("String", "inter_choose_avatar", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField ("String", "inter_choose_icon", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField ("String", "inter_choose_background", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField ("String", "inter_preview", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField ("String", "inter_preview_video", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField ("String", "inter_category", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField ("String", "native_save_theme", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField ("String", "native_set_call_theme", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField ("String", "collapsible_banner_home", "\"ca-app-pub-3940256099942544/2014213617\"")
            buildConfigField ("String", "rewarded_set_call_theme", "\"ca-app-pub-3940256099942544/5224354917\"")
        }
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)

    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.firebase.config.ktx)
    implementation(libs.billing.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.review.ktx)

    implementation(libs.circleimageview)
    implementation(libs.glide)
//    ksp("com.github.bumptech.glide:compiler:4.12.0")
    implementation(libs.gson)

    //Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.perf.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.messaging.ktx)

    implementation(libs.user.messaging.platform)

    implementation(libs.androidx.preference.ktx)
    implementation(libs.billing)
    implementation(libs.play.services.ads)
//    implementation 'com.google.android.play:core:1.10.3'
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.android.spinKit)
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.koin.android.compat)
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
    //admob
    implementation("com.github.VioTechDev:VioAds:0.0.5")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation(libs.shimmer)

    implementation(libs.joda.time)
    implementation(libs.timber)

    implementation(libs.lottie)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}