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

    // Apache Curator
    implementation(libs.curator.framework)
    implementation(libs.curator.recipes)
    implementation(libs.curator.client)

    // JUnit 5 测试框架
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

application {
    mainClass = "demo.zookeeper.client.ZooKeeperDemo"
}

tasks.register<JavaExec>("runServiceRegistryDemo") {
    group = "Execution"
    description = "Run the ServiceRegistryDemo"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.zookeeper.service.ServiceRegistryDemo")
}

tasks.register<JavaExec>("runClusterNodeNamingDemo") {
    group = "Execution"
    description = "Run the ClusterNodeNamingDemo"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.zookeeper.cluster.ClusterNodeNamingDemo")
}

tasks.register<JavaExec>("runSnowFlakeIdDemo") {
    group = "Execution"
    description = "Run the SnowFlakeIdDemo"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.zookeeper.snowflake.SnowFlakeIdDemo")
}

tasks.register<JavaExec>("runCuratorCacheDemo") {
    group = "Execution"
    description = "Run the CuratorCacheDemo"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.zookeeper.cache.CuratorCacheDemo")
}

tasks.register<JavaExec>("runReentrantLockDemo") {
    group = "Execution"
    description = "Run the ReentrantLockDemo"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.zookeeper.lock.ReentrantLockDemo")
}

tasks.register<JavaExec>("runZkLockDemo") {
    group = "Execution"
    description = "Run the ZkLockDemo"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.zookeeper.lock.ZkLockDemo")
}

tasks.register<JavaExec>("runZkLockV2Demo") {
    group = "Execution"
    description = "Run the ZkLockV2Demo"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.zookeeper.lock.ZkLockV2Demo")
}

tasks.register<JavaExec>("runConcurrentResourceAccessDemo") {
    group = "Execution"
    description = "Run the ConcurrentResourceAccessDemo"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.zookeeper.lock.ConcurrentResourceAccessDemo")
}
