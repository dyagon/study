
plugins {
    id("application")
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
    // rxjava
    implementation(libs.rxjava3)
    // implementation(libs.guava)
    // log
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)

}

application {
    // Define the main class for the application.
    // mainClass = "rxjava.integration.SiFileWatcherApp"
    mainClass = "demo.nio.App"
}

tasks.register<JavaExec>("runServer") {
    group = "Execution"
    description = "Run the server"
    classpath = sourceSets.main.get().runtimeClasspath 
    mainClass.set("demo.nio.echo.reactor2.MultiThreadReactorEchoServer")
    args = listOf("8080")
}

tasks.register<JavaExec>("runClient") {
    group = "Execution"
    description = "Run the client"
    classpath = sourceSets.main.get().runtimeClasspath 
    mainClass.set("demo.nio.echo.reactor.ReactorEchoClient")
    args = listOf("localhost", "8080")
    standardInput = System.`in`
}