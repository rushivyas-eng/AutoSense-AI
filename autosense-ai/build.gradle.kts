plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.autosense.ai.ai"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":autosense-api"))

    implementation(
        "com.google.ai.edge.litert:litert:1.4.1"
    )

    // JVM unit tests
    testImplementation(kotlin("test"))
    testImplementation(libs.junit)

    // Android instrumentation tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(
        "androidx.test:runner:1.7.0"
    )
}