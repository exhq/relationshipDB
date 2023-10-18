plugins {

    java
    application
}

repositories {
    mavenCentral()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    implementation("io.javalin:javalin:5.6.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.aventrix.jnanoid:jnanoid:2.0.0")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.7")
}

application {
    mainClass.set("dev.exhq.wedlock.Wedlock")
}

tasks.run.configure {
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
}
