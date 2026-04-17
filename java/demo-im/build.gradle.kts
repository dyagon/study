plugins {
    java
    id("com.google.protobuf") version "0.9.4"
    id("com.gradleup.shadow") version "9.2.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.netty.all)
    implementation(libs.protobuf.java)
    implementation(libs.protobuf.java.util)

    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)
    
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
}

tasks.register<JavaExec>("runServer") {
    group = "application"
    description = "Runs the IM Server"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("demo.im.server.ImServer")
    standardInput = System.`in`
}

tasks.register<JavaExec>("runClient") {
    group = "application"
    description = "Runs the IM Client"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("demo.im.client.ImClient")
    standardInput = System.`in`
}
