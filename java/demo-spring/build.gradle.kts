plugins {
    id("application")
    id("java")
}

group = "demo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)
    implementation(libs.spring.boot.starter)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.springBootStarterTest)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

application {
    mainClass = "demo.spring.spel.SpelDemoApplication"
}

tasks.register<JavaExec>("runSpelDemo") {
    group = "Execution"
    description = "Run SpEL demo (programmatic + @Value examples)"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.spring.spel.SpelDemoApplication")
}
