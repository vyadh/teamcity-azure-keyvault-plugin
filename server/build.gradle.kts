import com.github.rodm.teamcity.TeamCityEnvironment
import com.github.rodm.teamcity.TeamCityPluginExtension

plugins {
  kotlin("jvm")
  kotlin("kapt")
}

apply {
  plugin("com.github.rodm.teamcity-server")
}

extra["downloadsDir"] = project.findProperty("downloads.dir") ?: "$rootDir/downloads"
extra["serversDir"] = project.findProperty("servers.dir") ?: "$rootDir/servers"

val agent = configurations.getByName("agent")

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  compile(project(":common"))
  agent(project(path = ":agent", configuration = "plugin"))

  compileOnly("org.jetbrains.teamcity.internal:web:${rootProject.extra["teamcityVersion"]}")
  testCompile("org.jetbrains.teamcity.internal:web:${rootProject.extra["teamcityVersion"]}")
  compileOnly("org.jetbrains.teamcity.internal:server:${rootProject.extra["teamcityVersion"]}")

  compile("com.squareup.okhttp3:okhttp:3.11.0")
  testImplementation("com.squareup.okhttp3:mockwebserver:3.11.0")

  compile("com.squareup.moshi:moshi:1.8.0")
  compileOnly("com.squareup.moshi:moshi-kotlin-codegen:1.8.0")
  kapt("com.squareup.moshi:moshi-kotlin-codegen:1.8.0")

  testCompile("org.assertj:assertj-core:3.11.1")
  testCompile("org.junit.jupiter:junit-jupiter-api:5.3.1")
  testCompile("org.junit.jupiter:junit-jupiter-params:5.3.1")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.3.1")
  testCompile("com.nhaarman.mockitokotlin2:mockito-kotlin:2.0.0")
}

teamcity {
  version = rootProject.extra["teamcityVersion"] as String

  server {
    descriptor {
      name = "Azure Key Vault Support"
      displayName = "Azure Key Vault Support"
      version = rootProject.version as String?
      vendorName = "Kieron Wilkinson"
      vendorUrl = "https://github.com/vyadh"
      description = "Azure Key Vault Plugin for TeamCity"
      email = "kieron.wilkinson@gmail.com"
      useSeparateClassloader = true
    }
  }

  environments {
    downloadsDir = extra["downloadsDir"] as String
    baseHomeDir = extra["serversDir"] as String
    baseDataDir = "$rootDir/data"

    operator fun String.invoke(block: TeamCityEnvironment.() -> Unit) {
      environments.create(this, closureOf(block))
    }

    "teamcity2018" {
      version = "2018.1.2"
      serverOptions("-DTC.res.disableAll=true -Dteamcity.development.mode=true")
      agentOptions()
    }

    "teamcity2018Debug" {
      version = "2018.1.2"
      serverOptions("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5600 -DTC.res.disableAll=true -Dteamcity.development.mode=true")
      agentOptions("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5601")
    }
  }
}

// Extension function to allow cleaner configuration
fun Project.teamcity(configuration: TeamCityPluginExtension.() -> Unit) {
  configure(configuration)
}
