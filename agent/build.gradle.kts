import com.github.rodm.teamcity.TeamCityPluginExtension

plugins {
  kotlin("jvm")
  kotlin("kapt")
}

apply {
  plugin("com.github.rodm.teamcity-agent")
}

dependencies {
  compile(project(":common"))

  compile("com.squareup.okhttp3:okhttp:3.11.0")
  testImplementation("com.squareup.okhttp3:mockwebserver:3.11.0")

  compile("com.squareup.moshi:moshi:1.8.0")
  compileOnly("com.squareup.moshi:moshi-kotlin-codegen:1.8.0")
  kapt("com.squareup.moshi:moshi-kotlin-codegen:1.8.0")

  testCompile("org.assertj:assertj-core:3.11.1")
  testCompile("org.junit.jupiter:junit-jupiter-api:5.3.1")
  testCompile("org.junit.jupiter:junit-jupiter-params:5.3.1")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.3.1")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.3.1")
  testCompile("com.nhaarman.mockitokotlin2:mockito-kotlin:2.0.0")
}

configure<TeamCityPluginExtension> {
  version = rootProject.extra["teamcityVersion"] as String

  agent {
    descriptor {
      pluginDeployment {
        useSeparateClassloader = true
      }
    }
  }
}
