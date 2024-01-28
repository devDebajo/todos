import java.util.Properties
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.napier)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(project(":java-utils"))
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.activityCompose)
            implementation(libs.compose.uitooling)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.biometric)
            implementation(libs.accompanist.permissions)
        }

        jvmMain.dependencies {
            implementation(project(":java-utils"))
            implementation(compose.desktop.common)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.jvm)
        }

        iosMain.dependencies {}
    }
}

android {
    namespace = "ru.debajo.todos"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34

        applicationId = "ru.debajo.todos.androidApp"

        val properties = Properties()
        properties.load(rootProject.file("project.properties").inputStream())
        versionCode = properties.getProperty("versionNumber").toInt()
        versionName = properties.getProperty("packageVersion")
    }

    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/androidMain/resources")
        resources.srcDirs("src/commonMain/resources")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    signingConfigs {
        create("release") {
            keyAlias = rootProject.properties["RELEASE_KEY_ALIAS"] as? String
            keyPassword = rootProject.properties["RELEASE_KEY_PASSWORD"] as? String
            storeFile = file(rootProject.properties["RELEASE_STORE_FILE"] as String)
            storePassword = rootProject.properties["RELEASE_STORE_PASSWORD"] as? String
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs["release"]
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            val properties = Properties()
            properties.load(rootProject.file("project.properties").inputStream())
            packageVersion = properties.getProperty("packageVersion")
            packageName = properties.getProperty("appName")

            macOS {
                iconFile = project.file("src/commonMain/resources/logo.icns")
            }

            windows {
                iconFile = project.file("src/commonMain/resources/logo.ico")
            }

            linux {
                iconFile = project.file("src/commonMain/resources/logo.png")
            }
        }
    }
}

project.afterEvaluate {
    tasks.named<JavaExec>("run").configure {
        args = listOf("-d")
    }
}

buildConfig {
    packageName = "ru.debajo.todos.buildconfig"

    val properties = Properties()
    properties.load(rootProject.file("project.properties").inputStream())
    buildConfigField("String", "APP_VERSION", "\"${properties.getProperty("packageVersion")}\"")
    buildConfigField("Int", "VERSION_NUMBER", properties.getProperty("versionNumber"))
    buildConfigField("String", "DEVELOPER_NAME", "\"${properties.getProperty("developerName")}\"")
    buildConfigField("String", "DEVELOPER_EMAIL", "\"${properties.getProperty("developerEmail")}\"")
    buildConfigField("String", "APP_NAME", "\"${properties.getProperty("appName")}\"")
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
    kotlinOptions.freeCompilerArgs = listOf("-Xmulti-platform", "-Xexpect-actual-classes")
}
