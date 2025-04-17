import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.room)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists() && keystorePropertiesFile.canRead()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.muedsa.tvbox"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.muedsa.tvbox"
        minSdk = 24
        targetSdk = 35
        versionCode = 28
        versionName = "0.3.4"
    }

    signingConfigs {
        create("release") {
            if (keystoreProperties.containsKey("muedsa.signingConfig.storeFile")) {
                storeFile = file(keystoreProperties["muedsa.signingConfig.storeFile"] as String)
                storePassword = keystoreProperties["muedsa.signingConfig.storePassword"] as String
                keyAlias = keystoreProperties["muedsa.signingConfig.keyAlias"] as String
                keyPassword = keystoreProperties["muedsa.signingConfig.keyPassword"] as String
            } else {
                val debugSigningConfig = signingConfigs.getByName("debug")
                storeFile = debugSigningConfig.storeFile
                storePassword = debugSigningConfig.storePassword
                keyAlias = debugSigningConfig.keyAlias
                keyPassword = debugSigningConfig.keyPassword
            }
        }
    }

    defaultConfig {
        buildConfigField(
            "String",
            "DANDANPLAY_APP_ID",
            "\"${keystoreProperties["muedsa.danDanPlay.appId"]}\""
        )
        buildConfigField(
            "String",
            "DANDANPLAY_APP_SECRET",
            "\"${keystoreProperties["muedsa.danDanPlay.appSecret"]}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // 修改APK文件名
    applicationVariants.all {
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                outputFileName = "${rootProject.name}-${versionName}-${buildType.name}.apk"
            }
        }
    }
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.tv.material)

    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.datastore.preferences)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    implementation(libs.media3)
    implementation(libs.media3.ui)
    implementation(libs.media3.hls)

    implementation(libs.akdanmaku)

    implementation(libs.room)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    implementation(libs.paging)
    implementation(libs.paging.compose)

    implementation(project(":api"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
}

ksp {
    arg("room.generateKotlin", "true")
}

room {
    schemaDirectory("$projectDir/schemas")
}