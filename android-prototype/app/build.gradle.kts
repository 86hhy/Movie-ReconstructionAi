plugins {
    id("com.android.application")
}

android {
    namespace = "ai.cinerecreate.mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "ai.cinerecreate.mobile"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
}
