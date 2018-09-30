import com.github.rodm.teamcity.TeamCityPluginExtension

plugins {
  kotlin("jvm")
}

apply {
  plugin("com.github.rodm.teamcity-agent")
}

dependencies {
  compile(project(":common"))

  testCompile("org.assertj:assertj-core:3.11.1")
  testCompile("org.junit.jupiter:junit-jupiter-api:5.3.1")
  testCompile("org.junit.jupiter:junit-jupiter-params:5.3.1")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.3.1")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.3.1")
  testCompile("org.mockito:mockito-all:1.9.5")
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
