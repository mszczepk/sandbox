plugins {
   kotlin("jvm") version "2.4.0"
}

group = "mszczepk"
version = "1.0-SNAPSHOT"

repositories {
   mavenCentral()
}

dependencies {
   testImplementation(kotlin("test"))
   testImplementation("io.kotest:kotest-assertions-core-jvm:6.2.2")
}

kotlin {
   jvmToolchain(21)
}

tasks.test {
   useJUnitPlatform()
}