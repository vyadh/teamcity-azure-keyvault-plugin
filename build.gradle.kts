buildscript {
  repositories {
    mavenLocal()
    mavenCentral()
  }

  dependencies {
    classpath("com.github.rodm:gradle-teamcity-plugin:1.1.1")
  }
}

plugins {
  kotlin("jvm") version "1.2.0" apply false
}

group = "com.github.vyadh.teamcity"
version = "1.0-SNAPSHOT"

extra["teamcityVersion"] = findProperty("teamcity.version") ?: "2018.1.2"

subprojects {
  tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
      events("passed", "skipped", "failed")
    }
  }
}
