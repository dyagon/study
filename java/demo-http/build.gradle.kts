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
    // 日志框架
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)

    // Apache HttpClient v5
    implementation(libs.httpclient5)
    implementation(libs.httpcore5)

    // JUnit 5 测试框架
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

application {
    mainClass = "demo.http.HttpUrlConnectionClient"
}


tasks.register<JavaExec>("runClientBenchmark") {
    group = "Execution"
    description = "Run the HttpClientBenchmark"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.http.HttpClientBenchmark")
}

