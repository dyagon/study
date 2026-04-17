
plugins {
    id("application")
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.6"
    id("java") 
    id("groovy")
}

group = "demo"
version = "1.0-SNAPSHOT"

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // lombok
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    // resilience4j
    implementation(libs.resilience4j.all)

    // rxjava
    implementation(libs.rxjava3)
    implementation(libs.netty.codec.http)
    implementation(libs.reactor.netty.core)
    implementation(libs.reactor.netty.http)
    // implementation(libs.guava)

    // jackson
    implementation(libs.jackson.databind)
    implementation(libs.jackson.core)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.datatype.jdk8)
    implementation(libs.jackson.datatype.hibernate5)

    // apache camel
    implementation(libs.camel.core)
    implementation(libs.camel.main)
    implementation(libs.camel.file)

    // spring integration
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.integration)
    implementation(libs.spring.integration.file)

    // log
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)

    // test
    testImplementation(libs.groovy.all)
    testImplementation(libs.spock.core)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.platform.commons)
    testRuntimeOnly(libs.junit.platform.engine)
}

application {
    // Define the main class for the application.
    // mainClass = "rxjava.integration.SiFileWatcherApp"
    mainClass = "demo.rxjava.App"
}


tasks.withType<Test> {
    // 你提供的配置代码被移动到这里
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}