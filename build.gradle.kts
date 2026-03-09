description = "A Java client for KServe inference services."

plugins {
    `java-library`
    alias(libs.plugins.bakdata.release)
    alias(libs.plugins.bakdata.sonar)
    alias(libs.plugins.bakdata.sonatype)
    alias(libs.plugins.freefair.lombok)
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
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    implementation(libs.jsoup)
    implementation(libs.jackson.databind)
    implementation(libs.okhttp)
    implementation(libs.json)
    implementation(libs.resilience4j.retry)
    implementation(libs.slf4j.api)

    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockwebserver)

    testFixturesImplementation(libs.mockwebserver)
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
            id.set("jkbe")
        }
        developer {
            name.set("Philipp Schirmer")
            id.set("philipp94831")
        }
    }
}
