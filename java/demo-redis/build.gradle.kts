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
    implementation(libs.jedis)

    // Spring Data Redis (RedisTemplate)
    implementation(libs.springBootStarterDataRedis)
    implementation(libs.jackson.databind)

    // Redisson (distributed locks, collections, rate limit, bloom, pub/sub, etc.)
    implementation(libs.redisson)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.springBootStarterTest)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

// 启用 -parameters 编译选项，使 SpEL 表达式可以使用参数名（如 #id、#product）
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

application {
    mainClass = "demo.redis.jedis.JedisPoolDemo"
}

tasks.register<JavaExec>("runJedisPoolDemo") {
    group = "Execution"
    description = "Run JedisPoolDemo (requires Redis on localhost:6379)"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.redis.jedis.JedisPoolDemo")
}

tasks.register<JavaExec>("runRedisTemplateDemo") {
    group = "Execution"
    description = "Run Spring Data Redis RedisTemplate demo (requires Redis on localhost:6379)"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.redis.spring.RedisTemplateDemoApplication")
}

tasks.register<JavaExec>("runRedissonDemo") {
    group = "Execution"
    description = "Run Redisson demo (locks, collections, rate limit, delayed queue, ID, bloom, pub/sub)"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("demo.redis.redisson.RedissonDemoApplication")
}

tasks.register<Test>("cacheTimingTest") {
    group = "Verification"
    description = "Run cache hit timing breakdown test (Redis access vs serialization vs other)"
    filter { includeTestsMatching("demo.redis.spring.cache.CacheTimingTest.breakdownCacheHitTiming") }
}
