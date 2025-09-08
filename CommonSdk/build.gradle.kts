import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.aaraar)
}

android {
    namespace = "com.utils.live.commonsdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
}

dependencies {
    //runtimeOnly("org.jetbrains.kotlin:kotlinx-metadata-jvm:0.9.0")


    /*    implementation(
            files(
                fileTree(
                    mapOf(
                        "dir" to "${rootDir}/libs",
                        "include" to listOf("*.aar")
                    )
                )
            )
        )*/


    api(project(":androidremote"))
    api(project(":Connect-SDK"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)


    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.mqtt.client)
    implementation(libs.gson)
    implementation(libs.bcpkix.jdk15on)
    implementation(libs.roku.ecp.wrapper.kotlin)
    implementation(libs.protobuf.javalite)
    implementation(libs.kotlinx.serialization.json)


    //implementation("io.insert-koin:koin-bom:3.5.6")
    implementation(libs.koin.core)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.android)

// Or the latest stable version
}