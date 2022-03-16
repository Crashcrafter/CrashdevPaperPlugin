import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.crash"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://jitpack.io/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    implementation("org.jetbrains.exposed:exposed-core:0.37.3")
    implementation("org.jetbrains.exposed:exposed-dao:0.37.3")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.37.3")
    implementation("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    implementation("com.github.NuVotifier.NuVotifier:nuvotifier-bukkit:2.7.2")
    implementation("com.github.NuVotifier.NuVotifier:nuvotifier-api:2.7.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("com.github.KevinPriv:MojangAPI:1.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.10")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    dependencies {
        exclude(dependency("com.github.NuVotifier.NuVotifier::.*"))
        exclude(dependency("io.papermc.paper:paper-api"))
        exclude(dependency("org.ow2.asm::.*"))
        exclude(dependency("org.yaml:snakeyaml"))
        exclude(dependency("org.hamcrest:hamcrest-core"))
        exclude(dependency("org.checkerframework:checker-qual"))
        exclude(dependency("junit:junit"))
        exclude(dependency("net.kyori::.*"))
        exclude(dependency("org.slf4j:slf4j-api"))
        exclude(dependency("net.md-5::.*"))
        exclude(dependency("com.google.code.findbugs::.*"))
        exclude(dependency("com.google.code.gson::.*"))
        exclude(dependency("com.google.guava::.*"))
        exclude(dependency("com.google.googlecode.json-simple::.*"))
        exclude(dependency("commons-lang::.*"))
        exclude(dependency("it.unimi.dsi::.*"))
        exclude(dependency("org.apache.maven::.*"))
        exclude(dependency("org.apache.maven.resolver::.*"))
    }
    doLast {
        file("./build/libs/${project.name}-${project.version}-all.jar").copyTo(file("./server/plugins/${project.name}-${project.version}-all.jar"), overwrite = true)
    }
}