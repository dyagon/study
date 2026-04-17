plugins {
    id("java")
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runHashExamples") {
    group = "application"
    description = "Run hash examples"
    mainClass.set("HashExamples")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("runSymmetricExamples") {
    group = "application"
    description = "Run symmetric cryptography examples (AES-GCM, AES-CBC)"
    mainClass.set("SymmetricExamples")
    classpath = sourceSets["main"].runtimeClasspath
}


tasks.register<JavaExec>("runAsymmetricExamples") {
    group = "application"
    description = "Run asymmetric crypto examples (RSA, ECDSA, ECDH)"
    mainClass.set("AsymmetricExamples")
    classpath = sourceSets["main"].runtimeClasspath
}
