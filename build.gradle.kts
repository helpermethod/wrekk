plugins {
    kotlin("jvm") version "1.9.23"
    application
    id("org.graalvm.buildtools.native") version "0.10.1"
    id("com.diffplug.spotless") version "6.25.0"
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.gitlab4j:gitlab4j-api:5.5.0")
    implementation("com.github.ajalt.clikt:clikt:4.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.wiremock:wiremock:3.4.2")
}

application {
    mainClass = "io.github.helpermethod.plumber.PlumberKt"
}

graalvmNative {
    metadataRepository {
        enabled = true
    }
    agent {
        metadataCopy {
            mergeWithExisting = true
            inputTaskNames.add("test")
            outputDirectories.add("src/main/resources/META-INF/native-image")
        }
    }
    binaries {
        named("main") {
            buildArgs("--enable-http", "--enable-https")
        }
        named("test") {
            buildArgs(
                "--initialize-at-build-time=kotlin.annotation.AnnotationRetention,kotlin.annotation.AnnotationTarget",
                "--enable-http",
                "--enable-https"
            )
        }
    }
}

spotless {
    kotlin {
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
