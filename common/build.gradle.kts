import com.github.rodm.teamcity.TeamCityPluginExtension

plugins {
  kotlin("jvm")
}

apply {
  plugin("com.github.rodm.teamcity-common")
}

dependencies {
  compile(kotlin("stdlib"))

  testCompile("org.assertj:assertj-core:3.15.0")
  testCompile("org.junit.jupiter:junit-jupiter-api:5.6.0")
  testCompile("org.junit.jupiter:junit-jupiter-params:5.6.0")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.6.0")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

configure<TeamCityPluginExtension> {
  version = rootProject.extra["teamcityVersion"] as String
}
