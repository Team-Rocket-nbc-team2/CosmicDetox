import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.navigation.safeArgs)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.kotlin.parcelize)
}

val properties = Properties()

// Option B: keep key.properties at repo root (not under app/).
// key.properties is optional for debug builds, but required for release signing.
val keyPropertiesFile: File? = rootProject.file("key.properties")
if (keyPropertiesFile.exists()) {
    properties.load(FileInputStream(keyPropertiesFile!!))
}

val localPropertiesFile = project.rootProject.file("local.properties")
if (!localPropertiesFile.exists()) {
    throw GradleException("Missing local.properties at project root.")
}
properties.load(localPropertiesFile.inputStream())

android {
    namespace = "com.rocket.cosmic_detox"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rocket.cosmic_detox"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "1.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "DEFAULT_WEB_CLIENT_ID", "\"${properties["DEFAULT_WEB_CLIENT_ID"]}\"")
        buildConfigField("String", "KAKAO_APP_KEY", "\"${properties["KAKAO_APP_KEY"]}\"")
        resValue("string", "KAKAO_OAUTH_HOST", "\"${properties["KAKAO_OAUTH_HOST"]}\"")
    }

    signingConfigs {
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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // viewpager2
    implementation(libs.androidx.viewpager2)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.crashlytics)

    // Jetpack Navigation
    implementation(libs.bundles.navigation)
    implementation(libs.androidx.core)

    // hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.lifecycle.service)
    ksp(libs.hilt.android.compiler)

    // room
    implementation(libs.room)
    implementation(libs.room.ktx)
    annotationProcessor(libs.room.compiler)
    ksp(libs.room.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // glide
    implementation(libs.glide)

    // kakao login
    implementation(libs.kakao.v2.user)

    // google one tap login
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // startup
    implementation(libs.androidx.startup)

    // Timber
    implementation(libs.timber)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
