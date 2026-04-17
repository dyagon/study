plugins {
    id("application")
    id("java")
    id("groovy")
    id("com.google.protobuf") version "0.9.4"
}

group = "demo"
version = "1.0-SNAPSHOT"

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // netty
    implementation(libs.netty.all)
    // netty tcnative for SSL/TLS support
    // netty-tcnative-boringssl-static 应该包含所有平台的库
    // 如果遇到加载问题，可以明确指定平台 classifier
    implementation(libs.netty.tcnative.boringssl.static)
    // 明确添加 macOS ARM64 支持（如果需要）
    implementation("io.netty:netty-tcnative-boringssl-static:${libs.versions.netty.tcnative.get()}:osx-aarch_64")

    // lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // json
    implementation(libs.fastjson2)

    // protobuf
    implementation(libs.protobuf.java)
    implementation(libs.protobuf.java.util)

    // log
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)

    // JUnit 5 测试框架
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

}

application {
    // Define the main class for the application.
    // mainClass = "rxjava.integration.SiFileWatcherApp"
    mainClass = "demo.netty.App"
}

tasks.register<JavaExec>("runServer") {
    group = "Execution"
    description = "Run the server"
    classpath = sourceSets.main.get().runtimeClasspath
    args = listOf("8080")
    // mainClass.set("demo.netty.discard.DiscardServer")
    // mainClass.set("demo.netty.echo.EchoServer")
    mainClass.set("demo.netty.json.JsonServer")
}

tasks.register<JavaExec>("runClient") {
    group = "Execution"
    description = "Run the client"
    classpath = sourceSets.main.get().runtimeClasspath
    // mainClass.set("demo.netty.discard.DiscardClient")
    // mainClass.set("demo.netty.echo.EchoClient")
    mainClass.set("demo.netty.json.JsonClient")
    args = listOf("localhost", "8080")
}

// Protobuf 配置
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.28.2"
    }
    generateProtoTasks {
        ofSourceSet("main")
    }
}

// 添加 protobuf 运行任务
tasks.register<JavaExec>("runProtobufServer") {
    group = "Execution"
    description = "Run the protobuf server"
    classpath = sourceSets.main.get().runtimeClasspath
    args = listOf("8080")
    mainClass.set("demo.netty.protobuf.ProtobufServer")
}

tasks.register<JavaExec>("runProtobufClient") {
    group = "Execution"
    description = "Run the protobuf client"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.netty.protobuf.ProtobufClient")
    args = listOf("localhost", "8080")
}

// 添加 HTTP echo 运行任务
tasks.register<JavaExec>("runHttpEchoServer") {
    group = "Execution"
    description = "Run the HTTP echo server"
    classpath = sourceSets.main.get().runtimeClasspath
    args = listOf("8080")
    mainClass.set("demo.netty.http.HttpEchoServer")
}

tasks.register<JavaExec>("runHttpEchoClient") {
    group = "Execution"
    description = "Run the HTTP echo client"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.netty.http.HttpEchoClient")
    args = listOf("http://localhost:8080")
}

// 添加 HTTP/2 SSL echo 运行任务
tasks.register<JavaExec>("runHttp2EchoServer") {
    group = "Execution"
    description = "Run the HTTP/2 SSL echo server"
    classpath = sourceSets.main.get().runtimeClasspath
    args = listOf("8443")
    mainClass.set("demo.netty.http2.Http2EchoServer")
}

tasks.register<JavaExec>("runHttp2EchoClient") {
    group = "Execution"
    description = "Run the HTTP/2 SSL echo client"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.netty.http2.Http2EchoClient")
    args = listOf("https://localhost:8443")
}