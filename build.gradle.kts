plugins {
    kotlin("jvm") version "1.9.22"
    application
    id("org.graalvm.buildtools.native") version "0.9.28"
    id("com.diffplug.spotless") version "6.23.3"
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.gitlab4j:gitlab4j-api:5.5.0")
    implementation("com.github.ajalt.clikt:clikt:4.2.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.wiremock:wiremock:3.3.1")
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
            outputDirectories.add("src/main/resources/META-INF/native-image/plumber")
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
