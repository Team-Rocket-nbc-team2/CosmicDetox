import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
    alias(libs.plugins.google.services)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.dagger.hilt.android)
}

val keyPropertiesFile = rootProject.file("./app/key.properties")
val properties = Properties()
properties.load(FileInputStream(keyPropertiesFile))

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

    // Jetpack Navigation
    implementation(libs.bundles.navigation)
    implementation(libs.androidx.core)

    // hilt
    implementation (libs.hilt.android)
    ksp (libs.hilt.android.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}