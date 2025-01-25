import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("dev.hydraulic.conveyor") version "1.4"
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "seerbird.apps"

version = "1.1.2"

repositories {
    mavenCentral()
    google()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}
javafx {
    version = "19"
    modules("javafx.base")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }

    sourceSets {
        val jvmMain: KotlinSourceSet by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(compose.desktop.currentOs)
                implementation("commons-io:commons-io:2.6")
                implementation("org.apache.commons:commons-math3:3.0")
                implementation("org.apache.commons:commons-lang3:3.12.0")
                implementation("com.fazecast:jSerialComm:2.11.0")
                implementation("org.ejml:ejml-simple:0.39")
            }
        }
    }
}

dependencies {
    // Use the configurations created by the Conveyor plugin to tell Gradle/Conveyor where to find the artifacts for each platform.
    linuxAmd64(compose.desktop.linux_x64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)
}

compose.desktop {
    application {
        mainClass = "MainKt"
        jvmArgs("--add-opens=java.desktop/java.awt=ALL-UNNAMED",
                "-Djavax.accessibility.assistive_technologies",
                "-Djavax.accessibility.screen_magnifier_present=false"
        )

        //nativeDistributions {
        //    modules("java.naming", "java.sql", "jdk.jfr", "jdk.sctp", "jdk.unsupported")
        //}"""
    }
}

// region Work around temporary Compose bugs.
configurations.all {
    attributes {
        // https://github.com/JetBrains/compose-jb/issues/1404#issuecomment-1146894731
        attribute(Attribute.of("ui", String::class.java), "awt")
    }
}
tasks.compileJava {
    options.compilerArgs.add("-verbose")
}

dependencies {
    // Force override the Kotlin stdlib version used by Compose to 1.7 in the machine specific configurations, as otherwise we can end up
    // with a mix of 1.6 and 1.7 on our classpath. This is the same logic as is applied to the regular Compose configurations normally.
    val v = "1.7.10"
    for (m in setOf("linuxAmd64", "macAmd64", "macAarch64", "windowsAmd64")) {
        m("org.jetbrains.kotlin:kotlin-stdlib:$v")
        m("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$v")
        m("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$v")
    }
}
// endregion
