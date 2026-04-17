plugins {
    id("application")
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
}

group = "demo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Netty for WebSocket over HTTP
    implementation(libs.netty.all)

    // Logging
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)
}

application {
    // default entry; tasks below set specific mains
    mainClass = "demo.websocket.WebSocketEchoServer"
}

tasks.register<JavaExec>("runWebSocketServer") {
    group = "Execution"
    description = "Run the Netty WebSocket echo server"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.websocket.WebSocketEchoServer")
    args = listOf("8080")
}

tasks.register<JavaExec>("runWebSocketClient") {
    group = "Execution"
    description = "Run the Netty WebSocket echo client"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.websocket.WebSocketEchoClient")
    args = listOf("ws://localhost:8080/ws")
}

