plugins {
   alias(libs.plugins.kotlin)
}

group = "mszczepk"
version = "1.0.0"

repositories {
   mavenCentral()
}

dependencies {
   implementation(libs.spring.web)
   implementation(libs.jackson.module.kotlin)
   implementation(libs.kotlinx.coroutines)

   testImplementation(kotlin("test"))
   testImplementation(libs.bundles.tests.unit)
}

kotlin {
   jvmToolchain(21)
}

tasks.test {
   useJUnitPlatform()
}