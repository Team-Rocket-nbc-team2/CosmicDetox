import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
    alias(libs.plugins.google.services)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.kotlin.parcelize)
}

val keyPropertiesFile = rootProject.file("./app/key.properties")
val properties = Properties()
properties.load(FileInputStream(keyPropertiesFile))
properties.load(project.rootProject.file("local.properties").inputStream())

android {
    namespace = "com.rocket.cosmic_detox"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rocket.cosmic_detox"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "KAKAO_APP_KEY", properties.getProperty("KAKAO_APP_KEY"))
        resValue("string", "KAKAO_OAUTH_HOST", properties.getProperty("KAKAO_OAUTH_HOST"))
    }

    signingConfigs {
        getByName("debug") {
            storeFile = properties["storeFile"]?.toString()?.let { file(it) }
            storePassword = properties["storePassword"]?.toString()
            keyAlias = properties["keyAlias"]?.toString()
            keyPassword = properties["keyPassword"]?.toString()
        }
        create("release") {
            storeFile = properties["storeFile"]?.toString()?.let { file(it) }
            storePassword = properties["storePassword"]?.toString()
            keyAlias = properties["keyAlias"]?.toString()
            keyPassword = properties["keyPassword"]?.toString()
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.storage)

    // Jetpack Navigation
    implementation(libs.bundles.navigation)
    implementation(libs.androidx.core)

    // hilt
    implementation (libs.hilt.android)
    implementation(libs.androidx.lifecycle.service)
    ksp (libs.hilt.android.compiler)

    // glide
    implementation(libs.glide)

    // kakao login
    implementation(libs.kakao.v2.user)

    // google one tap login
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}