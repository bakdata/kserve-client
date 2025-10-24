description = "A Java client for KServe inference services."

plugins {
    `java-library`
    id("com.bakdata.release") version "1.11.1"
    id("com.bakdata.sonar") version "1.11.1"
    id("com.bakdata.sonatype") version "1.11.1"
    id("io.freefair.lombok") version "8.14.2"
    id("java-test-fixtures")
}

group = "com.bakdata.kserve"

tasks.withType<Test> {
    maxParallelForks = 4
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

dependencies {
    implementation(group = "org.jsoup", name = "jsoup", version = "1.18.3")
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.18.2")
    val okHttpVersion: String by project
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = okHttpVersion)
    implementation(group = "org.json", name = "json", version = "20250107")
    implementation(group = "io.github.resilience4j", name = "resilience4j-retry", version = "1.7.1")
    implementation(group = "org.slf4j", name = "slf4j-api", version = "2.0.17")

    val junitVersion: String by project
    testRuntimeOnly(group = "org.junit.platform", name = "junit-platform-launcher")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter", version = junitVersion)
    testImplementation(group = "org.assertj", name = "assertj-core", version = "3.27.6")
    testImplementation(group = "com.squareup.okhttp3", name = "mockwebserver", version = okHttpVersion)

    testFixturesImplementation(group = "com.squareup.okhttp3", name = "mockwebserver", version = okHttpVersion)
}

publication {
    developers {
        developer {
            name.set("Victor Künstler")
            id.set("VictorKuenstler")
        }
        developer {
            name.set("Alejandro Jaramillo")
            id.set("irux")
        }
        developer {
            name.set("Jakob Edding")
            id.set("jakob-ed")
        }
    }
}
